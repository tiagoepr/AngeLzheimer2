package com.example.angelzheimer2.extras;

public class Tasks {
    private String titulo;
    private String descricao;
    private String data;
    private String hora;
    //    private Date DataHora; // https://developer.android.com/reference/java/text/SimpleDateFormat
    private boolean enabled;
    private String ID;


    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

//    public SimpleDateFormat getDataHora() {
//        return DataHora;
//    }
//
//    public void setDataHora(SimpleDateFormat dataHora) {
//        DataHora = dataHora;
//    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
