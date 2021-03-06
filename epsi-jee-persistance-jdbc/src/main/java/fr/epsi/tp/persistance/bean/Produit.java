package fr.epsi.tp.persistance.bean;

import java.math.BigDecimal;

public class Produit extends AbstractPersistance<Long>{

  private String libelle;
  
  private String description;
  
  private BigDecimal prix;
  
  private Marque marque;

  public String getLibelle() {
    return libelle;
  }

  public void setLibelle(String libelle) {
    this.libelle = libelle;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrix() {
    return prix;
  }

  public void setPrix(BigDecimal prix) {
    this.prix = prix;
  }

  public Marque getMarque() {
    return marque;
  }

  public void setMarque(Marque marque) {
    this.marque = marque;
  }
  
  
}
