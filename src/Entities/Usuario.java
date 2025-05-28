/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import Enum.Role;
import java.util.Date;

/**
 *
 * @author Mauros
 */
public class Usuario {

    private String imageDir;
    private String usuario;
    private String nomeCompleto;
    private String senha;
    private String email;
    private Role role;
    private String acesso;

    public Usuario(String imageDir, String usuario, String nomeCompleto, String senha, String email, Role Role, String acesso) {
        this.imageDir = imageDir;
        this.usuario = usuario;
        this.nomeCompleto = nomeCompleto;
        this.senha = senha;
        this.email = email;
        this.role = Role;
        this.acesso = acesso;
    }

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role Role) {
        this.role = Role;
    }

    public String getAcesso() {
        return acesso;
    }

    public void setAcesso(String acesso) {
        this.acesso = acesso;
    }

}
