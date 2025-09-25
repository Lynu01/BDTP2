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
    {"Id": "1", "Nombre": "Cajero", "SalarioxHora": "11.00"},
    {"Id": "2", "Nombre": "Camarero", "SalarioxHora": "10.00"},
    {"Id": "3", "Nombre": "Cuidador", "SalarioxHora": "13.50"},
    {"Id": "4", "Nombre": "Conductor", "SalarioxHora": "15.00"},
    {"Id": "5", "Nombre": "Asistente", "SalarioxHora": "11.00"},
    {"Id": "6", "Nombre": "Recepcionista", "SalarioxHora": "12.00"},
    {"Id": "7", "Nombre": "Fontanero", "SalarioxHora": "13.00"},
    {"Id": "8", "Nombre": "Niñera", "SalarioxHora": "12.00"},
    {"Id": "9", "Nombre": "Conserje", "SalarioxHora": "11.00"},
    {"Id": "10", "Nombre": "Albañil", "SalarioxHora": "10.50"},
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

catalogos = [
    ("Puestos","Puesto",puestosCatalogo),
    ("TiposEvento","TipoEvento",tiposEventoCatalogo),
    ("TiposMovimientos","TipoMovimiento", tiposMovimientosCatalogo),
]

for contenedorTag, itemTag, data in catalogos:
    contenedorElem = ET.SubElement(root, contenedorTag)
    for item in data:
        # 'item' es un dict; sus claves se vuelven atributos del nodo
        ET.SubElement(contenedorElem, itemTag, item)

tree = ET.ElementTree(root)
output_file = "archivoDatos.xml"
tree.write(output_file, encoding="utf-8", xml_declaration=True)

print(f"Archivo XML generado exitosamente: {output_file}")