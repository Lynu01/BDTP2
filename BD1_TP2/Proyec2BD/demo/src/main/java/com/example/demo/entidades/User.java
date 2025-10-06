package com.example.demo.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "Usuario")
@NamedStoredProcedureQueries({
    @NamedStoredProcedureQuery(
        name = "User.login",
        procedureName = "sp_Login",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "nombre", type = String.class),
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "clave", type = String.class)
        },
        resultClasses = User.class
    ),
    @NamedStoredProcedureQuery(
        name = "User.logout",
        procedureName = "sp_Logout",
        parameters = {
            @StoredProcedureParameter(mode = ParameterMode.IN, name = "nombre", type = String.class)
        }
    )
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String clave;

    public User() {}

    public User(String nombre, String clave) {
        this.nombre = nombre;
        this.clave = clave;
    }


    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}
