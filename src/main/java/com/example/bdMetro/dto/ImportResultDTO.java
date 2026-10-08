package com.example.bdMetro.dto;

public class ImportResultDTO {

    private int imported;
    private int skipped;
    private int total;

    public ImportResultDTO(int imported, int skipped, int total) {
        this.imported = imported;
        this.skipped = skipped;
        this.total = total;
    }

    public int getImported() { return imported; }
    public void setImported(int imported) { this.imported = imported; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
