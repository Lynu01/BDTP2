package com.example.demo.controladores;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/", produces = "text/html")
    public String home() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>API de Empleados</title>
            </head>
            <body style="font-family: Arial, sans-serif; text-align: center; padding: 50px;">
                <h1>✅ Bienvenido a la API de Empleados</h1>
                <p>Puedes ver todos los empleados haciendo clic en el botón de abajo:</p>
                <form action="/empleados">
                    <button type="submit" style="padding: 10px 20px; font-size: 16px;">Ver Empleados</button>
                </form>
            </body>
            </html>
        """;
    }
}
