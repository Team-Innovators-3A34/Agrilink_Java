package org.example.demo.controller.backOffice.recyclingPoint;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.models.produit;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.produitService;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import org.example.demo.utils.sessionManager;

public class addproduit {

    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnUpload;

    @FXML
    private TextArea productdescription;

    @FXML
    private TextField productimage;

    @FXML
    private TextField productname;

    @FXML
    private TextField productquantity;

    @FXML
    private ComboBox<recyclingpoint> productrecyclingpointype;

    @FXML
    private Text title;

    private final produitService produitService = new produitService();
    private final recyclingpointService recyclingpointService = new recyclingpointService();
    private User user;
    private produit produitAModifier=null;

    public void setProduitPourModification(produit r) {
        this.produitAModifier = r;

        productname.setText(r.getNom());
        productimage.setText(r.getImage());
        productdescription.setText(r.getDescription());
        productquantity.setText(String.valueOf(r.getQuantity()));
        productrecyclingpointype.setValue(r.getRecyclingpoint());

        title.setText("Modifier votre produit");
    }

    @FXML
    public void initialize() {

        user = sessionManager.getInstance().getUser();
        productrecyclingpointype.getItems().addAll(recyclingpointService.rechercher());
        productrecyclingpointype.setConverter(new StringConverter<recyclingpoint>() {
            @Override
            public String toString(recyclingpoint rp) {
                return (rp != null) ? rp.getNom() : "";
            }
            @Override
            public recyclingpoint fromString(String string) {
                return productrecyclingpointype.getItems()
                        .stream()
                        .filter(rp -> rp.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }


    @FXML
    void addproduct(javafx.event.ActionEvent event) {
        String name = productname.getText();
        String image = productimage.getText();
        String description = productdescription.getText();
        String quantityStr = productquantity.getText();
        recyclingpoint selectedRecyclingPoint = productrecyclingpointype.getValue();
        if(produitAModifier != null) {
            System.out.println("test");
            produitAModifier.setNom(name);
            produitAModifier.setImage(image);
            produitAModifier.setDescription(description);
            produitAModifier.setQuantity(Integer.parseInt(quantityStr));
            produitAModifier.setRecyclingpoint(selectedRecyclingPoint);
            if (produitService.modifier(produitAModifier)) {
                HelloApplication.succes("✅ Produit modifie avec succès !");
                clearForm();
                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listProduit.fxml");
            } else {
                HelloApplication.error("❌ Erreur lors de la modification du produit.");
            }        }else {
            try {


                if (name.isEmpty() || quantityStr.isEmpty() || selectedRecyclingPoint == null) {
                    HelloApplication.error("⚠️ Veuillez remplir tous les champs obligatoires !");
                    return;
                }

                int quantity = Integer.parseInt(quantityStr);

                produit newProduct = new produit();
                newProduct.setNom(name);
                newProduct.setImage(image);
                newProduct.setDescription(description);
                newProduct.setQuantity(quantity);
                newProduct.setRecyclingpoint(selectedRecyclingPoint);
                newProduct.setUser_id(user.getId());

                if (produitService.ajouter(newProduct)) {
                    HelloApplication.succes("✅ Produit ajouté avec succès !");
                    clearForm();
                    HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listProduit.fxml");
                } else {
                    HelloApplication.error("❌ Erreur lors de l'ajout du produit.");
                }

            } catch (NumberFormatException e) {
                HelloApplication.error("❌ La quantité doit être un nombre entier.");
            } catch (Exception e) {
                HelloApplication.error("❌ Erreur : " + e.getMessage());
            }
        }
    }

    private void clearForm() {
        productname.clear();
        productimage.clear();
        productdescription.clear();
        productquantity.clear();
        productrecyclingpointype.setValue(null);
    }
}
