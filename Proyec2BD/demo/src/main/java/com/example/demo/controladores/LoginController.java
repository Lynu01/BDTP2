package com.example.demo.controladores;

import com.example.demo.dto.AttemptStatsDTO;
import com.example.demo.dto.ThrottleResultDTO;
import com.example.demo.entidades.User;
import com.example.demo.repositorios.UserRepository;
import com.example.demo.servicios.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired private UserRepository userRepository;
    @Autowired private LoginService loginService;

    @GetMapping("/")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String nombre,
                        @RequestParam String clave,
                        HttpServletRequest request,
                        RedirectAttributes ra) {

        String ip = request.getRemoteAddr();

        // 1) ¿Ya está bloqueado?
        ThrottleResultDTO pre = loginService.checkLoginThrottle(nombre, ip);
        if (pre.isBlocked()) {
            int secs = Math.max(0, pre.getSecondsRemaining());
            int mins = Math.max(1, (int) Math.ceil(secs / 60.0));
            ra.addFlashAttribute("error", "Usuario temporalmente bloqueado por intentos fallidos. Intenta de nuevo en ~" + mins + " minuto(s).");
            return "redirect:/";
        }

        // 2) Intento real (el SP se encarga de trazar *cada* fallo con "Intento N codigo: 5000X; usuario=<...>")
        Integer rc = userRepository.sp_Login(nombre, clave, ip);

        if (rc != null && rc == 0) {
            // Éxito → sesión y redirección sin banners
            request.getSession(true).setAttribute("usuarioActual", nombre);
            return "redirect:/empleados";
        }

        // 3) Credenciales inválidas → mostrar rojo + amarillo con N, M, K
        //    OJO: el SP ya dejó el intento; N ya incluye este fallo.
        AttemptStatsDTO stats = loginService.getAttemptStats(nombre, ip);
        int attempts  = stats.getAttempts5Min();  // N ya incluye el actual, NO sumar +1
        int max       = stats.getMaxAttempts();
        int remaining = Math.max(0, max - attempts);

        ra.addFlashAttribute("error", "Usuario o contraseña inválidos.");
        ra.addFlashAttribute("warn",
                "Intento " + attempts + " de " + max + " en los últimos 5 minutos. Te quedan "
                        + remaining + " intento(s) antes de bloqueo.");

        // 4) Chequear si con este fallo ya quedó bloqueado para mostrar el rojo de bloqueo
        ThrottleResultDTO post = loginService.checkLoginThrottle(nombre, ip);
        if (post.isBlocked()) {
            int secs = Math.max(0, post.getSecondsRemaining());
            int mins = Math.max(1, (int) Math.ceil(secs / 60.0));
            ra.addFlashAttribute("error", "Usuario temporalmente bloqueado por intentos fallidos. Intenta de nuevo en ~" + mins + " minuto(s).");
        }

        return "redirect:/";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "Registrarse";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre,
                                   @RequestParam String clave,
                                   @RequestParam String clave2) {
        if (!clave.equals(clave2)) {
            return "redirect:/registro?error=claves";
        }
        if (userRepository.findByNombre(nombre) != null) {
            return "redirect:/registro?error=usuario";
        }
        User nuevoUsuario = new User(nombre, clave);
        userRepository.save(nuevoUsuario);
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes ra) {
        request.getSession().invalidate();
        ra.addFlashAttribute("info", "Sesión cerrada.");
        return "redirect:/";
    }
}
