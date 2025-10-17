import random
import datetime
import xml.etree.ElementTree as ET
from faker import Faker

faker = Faker()
Faker.seed(12345)
random.seed(12345)

root = ET.Element("Datos");

# ---------------------------------------------------
# informacion de los catalogos
# ---------------------------------------------------

puestosCatalogo = [
    {"Nombre": "Cajero", "SalarioxHora": "11.00"},
    {"Nombre": "Camarero", "SalarioxHora": "10.00"},
    {"Nombre": "Cuidador", "SalarioxHora": "13.50"},
    {"Nombre": "Conductor", "SalarioxHora": "15.00"},
    {"Nombre": "Asistente", "SalarioxHora": "11.00"},
    {"Nombre": "Recepcionista", "SalarioxHora": "12.00"},
    {"Nombre": "Fontanero", "SalarioxHora": "13.00"},
    {"Nombre": "Niñera", "SalarioxHora": "12.00"},
    {"Nombre": "Conserje", "SalarioxHora": "11.00"},
    {"Nombre": "Albañil", "SalarioxHora": "10.50"},
]

tiposEventoCatalogo = [
    {"Id": "1", "Nombre": "Login Exitoso"},
    {"Id": "2", "Nombre": "Login No Exitoso"},
    {"Id": "3", "Nombre": "Login deshabilitado"},
    {"Id": "4", "Nombre": "Logout"},
    {"Id": "5", "Nombre": "Insercion no exitosa"},
    {"Id": "6", "Nombre": "Insercion exitosa"},
    {"Id": "7", "Nombre": "Update no exitoso"},
    {"Id": "8", "Nombre": "Update exitoso"},
    {"Id": "9", "Nombre": "Intento de borrado"},
    {"Id": "10", "Nombre": "Borrado exitoso"},
    {"Id": "11", "Nombre": "Consulta con filtro de nombre"},
    {"Id": "12", "Nombre": "Consulta con filtro de cedula"},
    {"Id": "13", "Nombre": "Intento de insertar movimiento"},
    {"Id": "14", "Nombre": "Insertar movimiento exitoso"},
]

tiposMovimientosCatalogo = [
    {"Id": "1", "Nombre": "Cumplir mes", "TipoAccion": "Credito"},
    {"Id": "2", "Nombre": "Bono vacacional", "TipoAccion": "Credito"},
    {"Id": "3", "Nombre": "Reversion Debito", "TipoAccion": "Credito"},
    {"Id": "4", "Nombre": "Disfrute de vacaciones", "TipoAccion": "Debito"},
    {"Id": "5", "Nombre": "Venta de vacaciones", "TipoAccion": "Debito"},
    {"Id": "6", "Nombre": "Reversion de Credito", "TipoAccion": "Debito"},
]

usuariosCatalogo = [
    {"Id": "1", "Nombre": "UsuarioScripts", "Pass": ")*2LnSr^lk"},
    {"Id": "2", "Nombre": "David", "Pass": "232rr^k"},
    {"Id": "3", "Nombre": "Alejandro", "Pass": "test"},
    {"Id": "4", "Nombre": "Esteban", "Pass": "contrasena"},
    {"Id": "5", "Nombre": "Daniel", "Pass": "himB9Dzd%_"},
    {"Id": "6", "Nombre": "Alex", "Pass": "24himAzzd%_65"},
    {"Id": "7", "Nombre": "Usuario No Valido", "Pass": "NoSoyValido"}
]

erroresCatalogo = [
    {"Id": "1","Codigo": "50001", "Descripcion": "Username no existe"},
    {"Id": "2","Codigo": "50002", "Descripcion": "Password no existe"},
    {"Id": "3","Codigo": "50003", "Descripcion": "Login deshabilitado"},
    {"Id": "4","Codigo": "50004", "Descripcion": "Empleado con ValorDocumentoIdentidad ya existe en inserción"},
    {"Id": "5","Codigo": "50005", "Descripcion": "Empleado con mismo nombre ya existe en inserción"},
    {"Id": "6","Codigo": "50006", "Descripcion": "Empleado con ValorDocumentoIdentidad ya existe en actualizacion"},
    {"Id": "7","Codigo": "50007", "Descripcion": "Empleado con mismo nombre ya existe en actualización"},
    {"Id": "8","Codigo": "50008", "Descripcion": "Error de base de datos"},
    {"Id": "9","Codigo": "50009", "Descripcion": "Nombre de empleado no alfabético"},
    {"Id": "10","Codigo": "50010", "Descripcion": "Valor de documento de identidad no alfabético"},
    {"Id": "11","Codigo": "50011", "Descripcion": "Monto del movimiento rechazado pues si se aplicar el saldo seria negativo."},
]

catalogos = [
    ("Puestos","Puesto",puestosCatalogo),
    ("TiposEvento","TipoEvento",tiposEventoCatalogo),
    ("TiposMovimientos","TipoMovimiento", tiposMovimientosCatalogo),
    ("Usuarios","usuario", usuariosCatalogo),
    ("Error","errorCodigo", erroresCatalogo)
]

for contenedorTag, itemTag, data in catalogos:
    contenedorElem = ET.SubElement(root, contenedorTag)
    for item in data:
        # 'item' es un dict; sus claves se vuelven atributos del nodo
        ET.SubElement(contenedorElem, itemTag, item)

# ---------------------------------------------------
#  catalogo de empleados para pruebas
# ---------------------------------------------------

numEmpleados = 20
empleados_elem = ET.SubElement(root, "Empleados")
empleados = []                 
empleados_doc_ids = set()      # unicidad del documento

for _ in range(numEmpleados):
    puesto_elegido = random.choice(puestosCatalogo)
    # doc único (7–8 dígitos)
    while True:
        valor_doc = str(faker.random_int(min=1_000_000, max=99_999_999))
        if valor_doc not in empleados_doc_ids:
            empleados_doc_ids.add(valor_doc)
            break

    nombre = faker.name()
    fecha_contratacion = faker.date_between(start_date="-10y", end_date="today").strftime("%Y-%m-%d")

    empleado = {
        "Puesto": puesto_elegido["Nombre"],    
        "ValorDocumentoIdentidad": valor_doc,
        "Nombre": nombre,
        "FechaContratacion": fecha_contratacion
    }
    empleados.append(empleado)

    ET.SubElement(empleados_elem, "empleado", empleado)

# ---------------------------------------------------
#  movimientos de prueba, seran de 1 a 3 por empleado
#  se añadieron 30 mas, esto por petición del profe
# ---------------------------------------------------

movimientos_elem = ET.SubElement(root, "Movimientos")
tipos_mov_nombres = [t["Nombre"] for t in tiposMovimientosCatalogo]
usuarios_nombres = [u["Nombre"] for u in usuariosCatalogo]
tiposMovId = [p["Id"] for p in tiposMovimientosCatalogo]  


for emp in empleados:
    fecha_emp = datetime.date.fromisoformat(emp["FechaContratacion"])
    for _ in range(random.randint(1, 3)):
        tipo = random.choice(tipos_mov_nombres)

        # fecha del movimiento no antes de la contratación
        delta = random.randint(0, max(1, (datetime.date.today() - fecha_emp).days))
        fecha_mov = faker.date_between_dates(
            date_start=datetime.date(2025,1,1),
            date_end=datetime.date(2025,12,31)
        )
        fecha_str = fecha_mov.isoformat()

        # hora aleatoria para PostTime el mismo día de 'Fecha'
        hh = random.randint(0, 23)
        mm = random.randint(0, 59)
        ss = random.randint(0, 59)
        post_time = f"{fecha_str} {hh:02d}:{mm:02d}:{ss:02d}"

        ET.SubElement(movimientos_elem, "movimiento", {
            "ValorDocId": emp["ValorDocumentoIdentidad"],
            "IdTipoMovimiento": random.choice(tiposMovId),
            "Fecha": fecha_str,
            "Monto": str(random.randint(1, 5)),
            "PostByUser": random.choice(usuarios_nombres), 
            "PostInIP": faker.ipv4(),
            "PostTime": post_time
        })
# ---------------------------------------------------
#  Serializado
# ---------------------------------------------------
tree = ET.ElementTree(root)
output_file = "archivoDatos.xml"
tree.write(output_file, encoding="utf-8", xml_declaration=True)
print(f"Archivo XML generado exitosamente: {output_file}")