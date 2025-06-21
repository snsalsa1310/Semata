package com.example.uaslabpbo.model;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UtangModel {
    private final StringProperty id;
    private final StringProperty keterangan;
    private final ObjectProperty<BigDecimal> jumlah;
    private final ObjectProperty<LocalDate> jatuhTempo;
    private final StringProperty status;
    private final ObjectProperty<LocalDate> dibuatPada;

    public UtangModel(String id, String keterangan, BigDecimal jumlah, LocalDate jatuhTempo, String status, LocalDate dibuatPada) {
        this.id = new SimpleStringProperty(id);
        this.keterangan = new SimpleStringProperty(keterangan);
        this.jumlah = new SimpleObjectProperty<>(jumlah);
        this.jatuhTempo = new SimpleObjectProperty<>(jatuhTempo);
        this.status = new SimpleStringProperty(status);
        this.dibuatPada = new SimpleObjectProperty<>(dibuatPada);
    }

    // Property Getters (dibutuhkan oleh PropertyValueFactory)
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty keteranganProperty() {
        return keterangan;
    }

    public StringProperty jumlahProperty() {
        // Format jumlah menjadi Rupiah untuk ditampilkan di tabel
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return new SimpleStringProperty(formatRupiah.format(jumlah.get()));
    }

    public StringProperty jatuhTempoProperty() {
        if (jatuhTempo.get() == null) return new SimpleStringProperty("-");
        return new SimpleStringProperty(jatuhTempo.get().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty dibuatPadaProperty() {
        if (dibuatPada.get() == null) return new SimpleStringProperty("-");
        return new SimpleStringProperty(dibuatPada.get().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
    }

    // Standard Getter
    public String getId() {
        return id.get();
    }
}