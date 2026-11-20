#!/usr/bin/env python3
"""K-33 - Corpus de pruebas determinista para Visor de Texto.

Uso:
    python generar_corpus.py --out corpus --large-gb 1
    python generar_corpus.py --verify corpus

Solo biblioteca estandar (Python >= 3.7), sin red.
Solo escribe dentro de --out; --verify es de solo lectura.
Codigo de salida: 0 = todo OK; 1 = verificacion con FALLO; 2 = error de E/S o argumentos.
"""
import argparse
import hashlib
import json
import os
import re
import shutil
import sys
import time
from collections import Counter
from decimal import Decimal, InvalidOperation
from pathlib import Path

# ---------------------------------------------------------------- constantes
LINE_LEN = 100                      # bytes por linea del archivo grande (con "\n")
PREFIJO_LEN = 14                    # "L" + 12 digitos + "|"
RELLENO_LEN = LINE_LEN - PREFIJO_LEN - 1
CHUNK_LINES = 100_000               # 10 MB por bloque
UNA_LINEA_BYTES = 10 * 1024 * 1024  # 10 MiB
BINARIO_BYTES = 4096
LINEA_A3 = 5_000_000

GRANDE = "grande_utf8.txt"
UNA_LINEA = "una_linea_10mb.txt"
VACIO = "vacio.txt"
MOJIBAKE_F = "roto_mojibake.txt"
SALTOS_F = "saltos_mixtos.txt"
BINARIO = "binario_disfrazado.txt"
SIN_EXT_F = "sin_extension"
DATOS_F = "datos.xyz"
MANIFEST = "manifest.json"
NOMBRES = [
    GRANDE, UNA_LINEA, VACIO,
    "tildes_utf8.txt", "tildes_latin1.txt", "tildes_utf8_bom.txt", "tildes_utf16le_bom.txt",
    MOJIBAKE_F, SALTOS_F, BINARIO, SIN_EXT_F, DATOS_F,
]
CLAVES = ("codificacion", "tipo", "lineas", "saltos", "ascii_puro")

BOM8 = b"\xef\xbb\xbf"
BOM16LE = b"\xff\xfe"
_BOMS = (BOM8, BOM16LE, b"\xfe\xff")

# ---------------------------------------------------------------- contenido
_ALFABETO = "abcdefghijklmnopqrstuvwxyz0123456789"
_RELLENOS = [(_ALFABETO * 4)[k:k + RELLENO_LEN] for k in range(len(_ALFABETO))]
_NREL = len(_RELLENOS)

LINEAS_TEXTO = [
    "¡Hola! ¿Cómo estás hoy?",
    "El niño pequeño comió piña y jamón en la montaña.",
    "La cigüeña voló sobre el pingüino sin vergüenza.",
    "¿Qué día es mañana? Mañana es lunes, según el calendario.",
    "Ayer escribí una canción sobre el año que pasó.",
    "¡Qué sorpresa! Nadie esperaba tanta alegría.",
    "El señor López compró café, azúcar y plátanos.",
    "¿Dónde está la biblioteca? Está allí, junto al jardín.",
    "La lingüística estudia cómo hablamos y cómo escribimos.",
    "¡Feliz cumpleaños! Que cumplas muchos años más.",
    "El camión salió de España con destino a Perú.",
    "Mi abuelo cuenta historias de sus días en el campo.",
    "¿Por qué llueve tanto en otoño? Pregúntaselo al viento.",
    "La música suena más bonita cuando todos cantan.",
    "Después de la lluvia salió el arco iris.",
    "¡Atención! El tren está por llegar a la estación.",
    "Pingüinos, cigüeñas y búhos viven en este cuento.",
    "¿Cuántos kilómetros faltan para llegar? Faltan pocos.",
    "Había una vez un rey que no sabía leer.",
    "¡Qué rico huele el pan recién horneado!",
    "También aprendimos que el corazón no entiende de razones.",
    "Mañana será otro día, y el sol saldrá por el oriente.",
    "¿Sabías que el murciélago es el único mamífero que vuela?",
    "Fin del texto de prueba: áéíóú ñ ü ¿? ¡!",
]
TEXTO = "\n".join(LINEAS_TEXTO) + "\n"
# UTF-8 leido erroneamente como Latin-1 y guardado de nuevo en UTF-8.
TEXTO_MOJIBAKE = TEXTO.encode("utf-8").decode("latin-1")
_FIN_SALTO = ("\n", "\r\n", "\r")
TEXTO_SALTOS = "".join(
    "Linea %02d de saltos mixtos%s" % (i + 1, _FIN_SALTO[i % 3]) for i in range(30)
)
TEXTO_SIN_EXT = "Archivo sin extension: texto ASCII corto.\n"
TEXTO_DATOS = "Datos de prueba UTF-8: año, canción, ¿qué tal?\n"

# nombre: (etiqueta de codificacion, codec Python, BOM, contenido canonico)
ARCH_TEXTO = {
    "tildes_utf8.txt": ("utf-8", "utf-8", b"", TEXTO),
    "tildes_latin1.txt": ("iso-8859-1", "latin-1", b"", TEXTO),
    "tildes_utf8_bom.txt": ("utf-8-bom", "utf-8", BOM8, TEXTO),
    "tildes_utf16le_bom.txt": ("utf-16le-bom", "utf-16-le", BOM16LE, TEXTO),
    MOJIBAKE_F: ("utf-8", "utf-8", b"", TEXTO_MOJIBAKE),
    SALTOS_F: ("ascii", "ascii", b"", TEXTO_SALTOS),
    SIN_EXT_F: ("ascii", "ascii", b"", TEXTO_SIN_EXT),
    DATOS_F: ("utf-8", "utf-8", b"", TEXTO_DATOS),
}

_RE_SALTOS = re.compile(r"\r\n|\r|\n")


def chunk_grande(primera, cantidad):
    """Lineas primera..primera+cantidad-1 del archivo grande (100 bytes c/u)."""
    return "".join(
        ["L%012d|%s\n" % (n, _RELLENOS[n % _NREL]) for n in range(primera, primera + cantidad)]
    ).encode("ascii")


def bytes_una_linea():
    base = b"0123456789abcdefghijklmnopqrstuvwxyz "
    return (base * (UNA_LINEA_BYTES // len(base) + 1))[:UNA_LINEA_BYTES]


def es_pe_valido(data):
    """True si 'data' tiene cabecera MZ y una firma PE\\0\\0 en e_lfanew (offset 0x3C)."""
    if len(data) < 0x40 or data[:2] != b"MZ":
        return False
    off = int.from_bytes(data[0x3C:0x40], "little")
    return 0 < off <= len(data) - 4 and data[off:off + 4] == b"PE\x00\x00"


def bytes_binario():
    """'MZ' + flujo pseudoaleatorio (SHA-256 con contador y semilla fija)."""
    for sal in range(1000):
        out = bytearray(b"MZ")
        contador = 0
        while len(out) < BINARIO_BYTES:
            out += hashlib.sha256(b"K-33|visor|%d|%d" % (sal, contador)).digest()
            contador += 1
        data = bytes(out[:BINARIO_BYTES])
        if b"\x00" in data and not es_pe_valido(data) and b"PE\x00\x00" not in data:
            return data
    raise RuntimeError("no se pudo generar el binario con las condiciones pedidas")


# ---------------------------------------------------------------- propiedades
def props_texto(texto):
    c = Counter(_RE_SALTOS.findall(texto))
    saltos = {"LF": c["\n"], "CRLF": c["\r\n"], "CR": c["\r"]}
    lineas = sum(saltos.values()) + (1 if texto and not texto.endswith(("\n", "\r")) else 0)
    return lineas, saltos, texto.isascii()


def _lf(n):
    return {"LF": n, "CRLF": 0, "CR": 0}


def esperado(nombre, nbytes):
    """Propiedades esperadas de cada archivo (fuente de verdad para manifest y --verify)."""
    if nombre == GRANDE:
        n = nbytes // LINE_LEN
        return dict(codificacion="utf-8", tipo="texto", lineas=n, saltos=_lf(n), ascii_puro=True)
    if nombre == UNA_LINEA:
        return dict(codificacion="utf-8", tipo="texto", lineas=1, saltos=_lf(0), ascii_puro=True)
    if nombre == VACIO:
        return dict(codificacion=None, tipo="texto", lineas=0, saltos=_lf(0), ascii_puro=True)
    if nombre == BINARIO:
        return dict(codificacion=None, tipo="binario", lineas=None, saltos=None, ascii_puro=False)
    if nombre == SALTOS_F:
        return dict(codificacion="ascii", tipo="texto", lineas=30,
                    saltos={"LF": 10, "CRLF": 10, "CR": 10}, ascii_puro=True)
    etiqueta, _codec, _bom, canon = ARCH_TEXTO[nombre]
    lineas, saltos, asc = props_texto(canon)
    return dict(codificacion=etiqueta, tipo="texto", lineas=lineas, saltos=saltos, ascii_puro=asc)


def _autochequeo():
    errs = []
    if len(chunk_grande(1, 1)) != LINE_LEN:
        errs.append("la linea del archivo grande no mide 100 bytes")
    if chunk_grande(LINEA_A3, 1)[:PREFIJO_LEN] != b"L000005000000|":
        errs.append("prefijo de la linea 5.000.000 incorrecto")
    if TEXTO.count("\n") < 20:
        errs.append("el texto tiene menos de 20 lineas")
    if not all(c in TEXTO for c in "áéíóúñü¿¡"):
        errs.append("al texto le faltan caracteres requeridos")
    try:
        TEXTO.encode("latin-1").decode("utf-8")
        errs.append("el texto Latin-1 tambien es UTF-8 valido")
    except UnicodeDecodeError:
        pass
    except UnicodeEncodeError:
        errs.append("el texto no es codificable en Latin-1")
    if "Ã¡" not in TEXTO_MOJIBAKE or "Ã±" not in TEXTO_MOJIBAKE:
        errs.append("el mojibake no contiene 'Ã¡' y 'Ã±'")
    if props_texto(TEXTO_SALTOS)[:2] != (30, {"LF": 10, "CRLF": 10, "CR": 10}):
        errs.append("saltos_mixtos no tiene 10 LF + 10 CRLF + 10 CR")
    if not TEXTO_SIN_EXT.isascii():
        errs.append("sin_extension no es ASCII")
    bytes_binario()
    if errs:
        raise SystemExit("Autochequeo fallido: " + "; ".join(errs))


# ---------------------------------------------------------------- generacion
def _escribir(ruta, trozos):
    """Escribe a un temporal exclusivo y renombra; devuelve (bytes, sha256)."""
    tmp = ruta.with_name(ruta.name + ".tmp")
    if os.path.lexists(tmp):
        os.unlink(tmp)
    h = hashlib.sha256()
    n = 0
    try:
        with open(tmp, "xb") as f:      # "x": falla si existe, no sigue enlaces
            for t in trozos:
                f.write(t)
                h.update(t)
                n += len(t)
        os.replace(tmp, ruta)
    except BaseException:
        try:
            if os.path.lexists(tmp):
                os.unlink(tmp)
        except OSError:
            pass
        raise
    return n, h.hexdigest()


def _trozos_grande(total):
    hecho = 0
    siguiente = 10
    while hecho < total:
        k = min(CHUNK_LINES, total - hecho)
        yield chunk_grande(hecho + 1, k)
        hecho += k
        pct = hecho * 100 // total
        if pct >= siguiente:
            print(f"    ... {pct}%", flush=True)
            siguiente = (pct // 10 + 1) * 10


def generar(out, large_bytes):
    if out.exists() and not out.is_dir():
        raise OSError(f"--out existe y no es un directorio: {out}")
    out.mkdir(parents=True, exist_ok=True)
    necesario = large_bytes + UNA_LINEA_BYTES + (1 << 20)
    libre = shutil.disk_usage(out).free
    if libre < necesario:
        raise OSError(f"espacio insuficiente: se necesitan ~{necesario:,} bytes y hay {libre:,} libres")

    info = {}

    def hacer(nombre, trozos):
        n, sha = _escribir(out / nombre, trozos)
        info[nombre] = (n, sha)
        print(f"  generado {nombre}: {n:,} bytes")

    hacer(GRANDE, _trozos_grande(large_bytes // LINE_LEN))
    hacer(UNA_LINEA, [bytes_una_linea()])
    hacer(VACIO, [b""])
    for nombre, (_etq, codec, bom, canon) in ARCH_TEXTO.items():
        hacer(nombre, [bom + canon.encode(codec)])
    hacer(BINARIO, [bytes_binario()])

    entradas = []
    for nombre in NOMBRES:
        n, sha = info[nombre]
        e = {"nombre": nombre, "bytes": n, "sha256": sha}
        e.update(esperado(nombre, n))
        entradas.append(e)
    manifiesto = {"tarjeta": "K-33", "version": 1, "archivos": entradas}
    _escribir(out / MANIFEST, [(json.dumps(manifiesto, indent=2, sort_keys=True) + "\n").encode("ascii")])
    print(f"  generado {MANIFEST}")


# ---------------------------------------------------------------- verificacion
def _decodificar(data, codec, errores):
    try:
        return data.decode(codec)
    except UnicodeDecodeError as exc:
        errores.append(f"no decodifica como {codec}: {exc}")
        return None


def _medido_texto(texto, etiqueta):
    if texto is None:
        return dict(codificacion="invalida", tipo="binario", lineas=None, saltos=None, ascii_puro=False)
    lineas, saltos, asc = props_texto(texto)
    tipo = "binario" if "\x00" in texto else "texto"
    return dict(codificacion=etiqueta, tipo=tipo, lineas=lineas, saltos=saltos, ascii_puro=asc)


def _medir_pequeno(nombre, data, errores):
    if nombre == VACIO:
        if data:
            errores.append("debe tener 0 bytes")
        return _medido_texto("", None)

    if nombre == BINARIO:
        if len(data) != BINARIO_BYTES:
            errores.append(f"debe medir {BINARIO_BYTES} bytes")
        if data[:2] != b"MZ":
            errores.append("no empieza con 4D 5A")
        if es_pe_valido(data):
            errores.append("parece un ejecutable PE valido")
        return dict(codificacion=None, tipo="binario" if b"\x00" in data else "texto",
                    lineas=None, saltos=None, ascii_puro=data.isascii())

    if nombre == UNA_LINEA:
        if len(data) != UNA_LINEA_BYTES:
            errores.append(f"debe medir {UNA_LINEA_BYTES} bytes")
        if b"\n" in data or b"\r" in data:
            errores.append("contiene saltos de linea")
        if data != bytes_una_linea():
            errores.append("contenido distinto del esperado")
        return _medido_texto(_decodificar(data, "utf-8", errores), "utf-8")

    etiqueta, codec, bom, canon = ARCH_TEXTO[nombre]
    if bom:
        if not data.startswith(bom):
            errores.append("falta la BOM")
    elif data.startswith(_BOMS):
        errores.append("tiene una BOM inesperada")
    cuerpo = data[len(bom):] if bom and data.startswith(bom) else data
    if codec == "utf-16-le" and len(cuerpo) % 2:
        errores.append("longitud impar para UTF-16")
    texto = _decodificar(cuerpo, codec, errores)
    if codec == "latin-1":
        try:
            cuerpo.decode("utf-8")
            errores.append("tambien es UTF-8 valido (no sirve como caso Latin-1)")
        except UnicodeDecodeError:
            pass
    if texto is not None:
        if texto != canon:
            errores.append("contenido distinto del esperado")
        if nombre.startswith("tildes_"):
            if props_texto(texto)[0] < 20:
                errores.append("menos de 20 lineas")
            faltan = [c for c in "áéíóúñü¿¡" if c not in texto]
            if faltan:
                errores.append("faltan caracteres: " + "".join(faltan))
        if nombre == MOJIBAKE_F:
            if "Ã¡" not in texto or "Ã±" not in texto:
                errores.append("no aparecen 'Ã¡' y 'Ã±'")
            try:
                if texto.encode("latin-1").decode("utf-8") != TEXTO:
                    errores.append("el mojibake no revierte al texto original")
            except (UnicodeEncodeError, UnicodeDecodeError):
                errores.append("el mojibake no revierte al texto original")
    return _medido_texto(texto, etiqueta)


def _medir_grande(ruta, size, errores):
    h = hashlib.sha256()
    lf = cr = linea = 0
    todo_ascii, hay_nul = True, False
    estructura_ok = contenido_ok = True
    ultimo = b""
    with open(ruta, "rb") as f:
        while True:
            chunk = f.read(CHUNK_LINES * LINE_LEN)
            if not chunk:
                break
            h.update(chunk)
            k = len(chunk) // LINE_LEN
            nl = chunk.count(b"\n")
            lf += nl
            cr += chunk.count(b"\r")
            ultimo = chunk[-1:]
            if not chunk.isascii():
                todo_ascii = False
            if b"\x00" in chunk:
                hay_nul = True
            if len(chunk) % LINE_LEN or nl != k or chunk[LINE_LEN - 1::LINE_LEN] != b"\n" * k:
                estructura_ok = False
            elif contenido_ok and chunk != chunk_grande(linea + 1, k):
                contenido_ok = False
                errores.append(f"numeracion/relleno distintos del esperado cerca de la linea {linea + 1}")
            linea += k
    if not estructura_ok:
        errores.append("estructura: cada linea debe medir 100 bytes y terminar en LF")
    if size // LINE_LEN >= LINEA_A3:
        with open(ruta, "rb") as f:
            f.seek((LINEA_A3 - 1) * LINE_LEN)
            if f.read(PREFIJO_LEN) != b"L%012d|" % LINEA_A3:
                errores.append("A3: la linea 5.000.000 no empieza con 'L000005000000|'")
    lineas = lf + (0 if size == 0 or ultimo == b"\n" else 1)
    medido = dict(codificacion="utf-8" if todo_ascii else "invalida",
                  tipo="binario" if hay_nul else "texto", lineas=lineas,
                  saltos={"LF": lf, "CRLF": 0, "CR": cr}, ascii_puro=todo_ascii)
    return medido, h.hexdigest()


def verificar_archivo(base, nombre, entrada):
    ruta = base / nombre
    if ruta.is_symlink() or not ruta.is_file():
        return ["no existe o no es un archivo regular"]
    nb = entrada.get("bytes")
    if not isinstance(nb, int) or isinstance(nb, bool) or nb < 0:
        return ["manifest: 'bytes' invalido"]
    errores = []
    size = ruta.stat().st_size
    if size != nb:
        errores.append(f"tamano {size} distinto del manifest ({nb})")
    if nombre == GRANDE and (size <= 0 or size % LINE_LEN):
        errores.append("el tamano debe ser > 0 y multiplo de 100")
    esp = esperado(nombre, nb)
    if nombre == GRANDE:
        medido, sha = _medir_grande(ruta, size, errores)
    else:
        data = ruta.read_bytes()
        sha = hashlib.sha256(data).hexdigest()
        medido = _medir_pequeno(nombre, data, errores)
    if entrada.get("sha256") != sha:
        errores.append("sha256 distinto del manifest")
    for k in CLAVES:
        if medido[k] != esp[k]:
            errores.append(f"{k}: medido {medido[k]!r}, esperado {esp[k]!r}")
        if entrada.get(k) != esp[k]:
            errores.append(f"manifest.{k} = {entrada.get(k)!r}, esperado {esp[k]!r}")
    return errores


def verificar(base):
    try:
        man = json.loads((base / MANIFEST).read_text(encoding="utf-8"))
        lista = man["archivos"]
        entradas = {e["nombre"]: e for e in lista}
        if len(entradas) != len(lista):
            raise ValueError("nombres duplicados en el manifest")
    except (OSError, ValueError, KeyError, TypeError) as exc:
        print(f"FALLO {MANIFEST}: {exc}")
        return False
    print(f"OK    {MANIFEST}")
    todo_ok = True
    for nombre in NOMBRES:
        if nombre not in entradas:
            print(f"FALLO {nombre}: no figura en el manifest")
            todo_ok = False
            continue
        try:
            errores = verificar_archivo(base, nombre, entradas[nombre])
        except OSError as exc:
            errores = [f"error de lectura: {exc}"]
        if errores:
            todo_ok = False
            print(f"FALLO {nombre}: " + "; ".join(errores))
        else:
            print(f"OK    {nombre}")
    desconocidos = sorted(set(entradas) - set(NOMBRES))
    if desconocidos:
        todo_ok = False
        print(f"FALLO {MANIFEST}: entradas inesperadas: {', '.join(desconocidos)}")
    if base.is_dir():
        extras = sorted(p.name for p in base.iterdir() if p.name not in NOMBRES and p.name != MANIFEST)
        if extras:
            print(f"AVISO archivos ajenos al corpus (ignorados): {', '.join(extras)}")
    print("RESULTADO: " + ("OK" if todo_ok else "FALLO"))
    return todo_ok


# ---------------------------------------------------------------- CLI
def _gb(valor):
    try:
        d = Decimal(valor)
    except InvalidOperation:
        raise argparse.ArgumentTypeError(f"valor invalido: {valor!r}")
    if not d.is_finite() or d <= 0:
        raise argparse.ArgumentTypeError("debe ser un numero > 0")
    total = d * 1_000_000_000
    if total != total.to_integral_value() or int(total) % LINE_LEN or int(total) // LINE_LEN >= 10 ** 12:
        raise argparse.ArgumentTypeError(
            "large-gb x 1.000.000.000 debe ser entero, multiplo de 100 y menor que 10^14 bytes")
    return int(total)


def resumen(base, t0):
    total = disco = 0
    for nombre in NOMBRES + [MANIFEST]:
        try:
            st = (base / nombre).lstat()
        except OSError:
            continue
        total += st.st_size
        disco += getattr(st, "st_blocks", 0) * 512
    print(f"Tiempo total: {time.perf_counter() - t0:.2f} s")
    linea = f"Espacio ocupado: {total:,} bytes ({total / 2 ** 30:.3f} GiB)"
    if disco:
        linea += f"; en disco: {disco:,} bytes"
    print(linea)


def main(argv=None):
    t0 = time.perf_counter()
    for s in (sys.stdout, sys.stderr):
        try:
            s.reconfigure(errors="replace")
        except (AttributeError, ValueError):
            pass
    ap = argparse.ArgumentParser(description="K-33: corpus de pruebas determinista y verificacion.")
    g = ap.add_mutually_exclusive_group(required=True)
    g.add_argument("--out", metavar="DIR", help="directorio donde generar el corpus")
    g.add_argument("--verify", metavar="DIR", help="verifica un corpus ya generado")
    ap.add_argument("--large-gb", dest="large_bytes", type=_gb, default=None, metavar="N",
                    help="tamano de grande_utf8.txt = N x 1.000.000.000 bytes (por defecto 1)")
    args = ap.parse_args(argv)
    _autochequeo()
    try:
        if args.verify is not None:
            if args.large_bytes is not None:
                print("AVISO: --large-gb se ignora con --verify")
            base = Path(args.verify)
            ok = verificar(base)
            resumen(base, t0)
            return 0 if ok else 1
        out = Path(args.out)
        generar(out, args.large_bytes if args.large_bytes is not None else 1_000_000_000)
        resumen(out, t0)
        return 0
    except OSError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())