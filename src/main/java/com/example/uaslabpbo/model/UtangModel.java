package com.example.uaslabpbo.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UtangModel {
    private final StringProperty id;
    private final StringProperty keterangan;
    private final StringProperty jumlah;
    private final StringProperty jatuhTempo;
    private final StringProperty status;
    private final StringProperty dibuatPada;

    public UtangModel(String id, String keterangan, BigDecimal jumlah, LocalDate jatuhTempo, boolean statusLunas, LocalDate dibuatPada) {
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        this.id = new SimpleStringProperty(id);
        this.keterangan = new SimpleStringProperty(keterangan);
        this.jumlah = new SimpleStringProperty(formatRupiah.format(jumlah));
        this.jatuhTempo = new SimpleStringProperty(jatuhTempo != null ? jatuhTempo.format(dateFormat) : "N/A");
        this.status = new SimpleStringProperty(statusLunas ? "Lunas" : "Belum Lunas");
        this.dibuatPada = new SimpleStringProperty(dibuatPada != null ? dibuatPada.format(dateFormat) : "N/A");
    }

    // Getters untuk properties (dibutuhkan oleh TableView)
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    public StringProperty keteranganProperty() { return keterangan; }
    public StringProperty jumlahProperty() { return jumlah; }
    public StringProperty jatuhTempoProperty() { return jatuhTempo; }
    public StringProperty statusProperty() { return status; }
    public StringProperty dibuatPadaProperty() { return dibuatPada; }
}