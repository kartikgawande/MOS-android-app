package com.example.mos.ui.notes.notesRV;

public class NoteItemModel {
    String classification, category, state, content;

    public String getDbRowNo() {
        return dbRowNo;
    }

    public void setDbRowNo(String dbRowNo) {
        this.dbRowNo = dbRowNo;
    }

    String dbRowNo;

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
