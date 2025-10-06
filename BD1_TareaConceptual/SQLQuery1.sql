CREATE TABLE dbo.Empleado  (
id INT IDENTITY (1, 1) PRIMARY KEY
,Nombre VARCHAR(128) NOT NULL
,Salario MONEY NOT NULL
); 

SELECT * FROM Empleado;

INSERT INTO dbo.Empleado (Nombre, Salario) VALUES 
('Carlos Ramírez', 250000.00),
('María González', 260500.00),
('Pedro Fernández', 275000.00),
('Laura Méndez', 280750.00),
('José Rojas', 290300.00),
('Ana Vargas', 305200.00),
('Luis Castro', 310000.00),
('Sofía Herrera', 315600.00),
('Jorge Salazar', 320000.00),
('Andrea Solís', 330250.00),
('Daniel Morales', 340700.00),
('Gabriela Pineda', 355000.00),
('Ricardo Soto', 365300.00),
('Fernanda López', 370800.00),
('Esteban Jiménez', 385000.00),
('Carolina Navarro', 390200.00),
('Roberto Aguilar', 400000.00),
('Natalia Campos', 410600.00),
('Héctor Valverde', 420750.00),
('Isabela Céspedes', 430500.00),
('Manuel Alvarado', 445000.00),
('Luisa Carrillo', 455300.00),
('Felipe Orozco', 460800.00),
('Camila Duarte', 470000.00),
('Martín Araya', 480500.00),
('Paola Espinoza', 495000.00),
('Diego Gutiérrez', 505200.00),
('Valeria Sandoval', 515700.00),
('Adrián Cordero', 525600.00),
('Mariana Chaves', 530300.00),
('Gustavo Quirós', 540800.00),
('Fabiola Vega', 550500.00),
('Rodrigo Molina', 565000.00),
('Elena Zúñiga', 575200.00),
('Álvaro Acuña', 580750.00),
('Daniela Barrantes', 595000.00),
('Sebastián Leiva', 605300.00),
('Verónica Méndez', 615600.00),
('Mauricio Pacheco', 630250.00),
('Raquel Ulate', 645000.00), 
('Kevin Alanis', 400000.00),
('Julia Harlander', 500000.00), 
('Florian Martinez', 600000.00), 
('Gaudy Obando', 700000.00), 
('Nella Jimenez', 120000.00), 
('Fausto Jara', 310000.00), 
('Ronja Torres', 320000.00), 
('Lupita Alpizar', 344000.00), 
('Jose Cedeño', 560000.00); 
 
