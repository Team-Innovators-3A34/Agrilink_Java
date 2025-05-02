package org.example.demo.services.posts;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.example.demo.models.Posts;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.utils.ConfigUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProfilePdfGeneratorService {

    private static final float MARGIN = 36f;

    public String generateProfilePdf(User user, List<Posts> posts, List<Ressources> resources) throws DocumentException, IOException {
        // esm lfile
        String fileName = generateFileName(user);
        String outputPath = ConfigUtil.getTemporaryFilePath() + "/" + fileName;

        // Create PDF document
        Document document = new Document(PageSize.A4);
        document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        // contenu
        addHeader(document, user);
        addContactInformation(document, user);
        addInvestorNotes(document, user);
        addResources(document, resources);
        addPosts(document, posts);
        document.close();

        return outputPath;
    }

    private String generateFileName(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        return String.format(
                "profile_%s_%s_%s.pdf",
                user.getNom().toLowerCase(),
                user.getPrenom().toLowerCase(),
                LocalDateTime.now().format(formatter)
        );
    }

    private void addHeader(Document document, User user) throws DocumentException, IOException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new BaseColor(0, 102, 255)); // Blue background
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
        Paragraph title = new Paragraph(user.getNom() + " " + user.getPrenom(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(title);

        // date
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.WHITE);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        Paragraph generatedDate = new Paragraph("Generated on " + LocalDateTime.now().format(formatter), dateFont);
        generatedDate.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(generatedDate);

        // profile image
        if(user.getImage() != null && !user.getImage().isEmpty()) {
            try {
                Image image = Image.getInstance(user.getImage());
                image.setAlignment(Element.ALIGN_CENTER);
                image.scaleToFit(120, 120);

                image.setAlignment(Element.ALIGN_CENTER);
                headerCell.addElement(image);
            } catch (Exception e) {
                // Image not available
            }
        }

        headerTable.addCell(headerCell);
        document.add(headerTable);
    }

    private void addContactInformation(Document document, User user) throws DocumentException {
        document.add(new Paragraph(" "));

        PdfPTable contactTable = new PdfPTable(1);
        contactTable.setWidthPercentage(100);
        contactTable.getDefaultCell().setBorder(Rectangle.BOX);
        contactTable.getDefaultCell().setBackgroundColor(new BaseColor(240, 248, 255)); // Light blue background
        contactTable.getDefaultCell().setPadding(10);

        // user information
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph contactTitle = new Paragraph("Contact Information", sectionFont);

        // user details
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph nameInfo = new Paragraph("Name: " + user.getNom() + " " + user.getPrenom(), normalFont);
        Paragraph emailInfo = new Paragraph("Email: " + user.getEmail(), normalFont);
        Paragraph phoneInfo = new Paragraph("Phone: " + user.getTelephone(), normalFont);
        Paragraph addressInfo = new Paragraph("Address: " + user.getAdresse(), normalFont);

        PdfPCell contactCell = new PdfPCell();
        contactCell.setBorder(Rectangle.BOX);
        contactCell.setBackgroundColor(new BaseColor(240, 248, 255));
        contactCell.addElement(contactTitle);
        contactCell.addElement(nameInfo);
        contactCell.addElement(emailInfo);
        contactCell.addElement(phoneInfo);
        contactCell.addElement(addressInfo);

        contactTable.addCell(contactCell);
        document.add(contactTable);
    }

    private void addInvestorNotes(Document document, User user) throws DocumentException {
        if(user.getDescription() != null && !user.getDescription().isEmpty()) {
            document.add(new Paragraph(" "));

            PdfPTable notesTable = new PdfPTable(1);
            notesTable.setWidthPercentage(100);

            PdfPCell notesCell = new PdfPCell();
            notesCell.setBorder(Rectangle.BOX);
            notesCell.setBackgroundColor(new BaseColor(255, 248, 220));
            notesCell.setPadding(10);

            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph notesTitle = new Paragraph("Investor Notes", sectionFont);

            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);
            Paragraph notesContent = new Paragraph(user.getDescription(), normalFont);

            notesCell.addElement(notesTitle);
            notesCell.addElement(notesContent);

            notesTable.addCell(notesCell);
            document.add(notesTable);
        }
    }

    private void addResources(Document document, List<Ressources> resources) throws DocumentException, IOException {
        if(resources != null && !resources.isEmpty()) {
            document.add(new Paragraph(" "));

            // Resources header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
            PdfPTable resourceHeaderTable = new PdfPTable(1);
            resourceHeaderTable.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell(new Paragraph("Resources", headerFont));
            headerCell.setBackgroundColor(new BaseColor(0, 102, 255)); // Blue background
            headerCell.setPadding(10);
            headerCell.setBorder(Rectangle.NO_BORDER);

            resourceHeaderTable.addCell(headerCell);
            document.add(resourceHeaderTable);

            // Resources content
            PdfPTable resourcesTable = new PdfPTable(3); // 3 columns for icon, name, and type
            resourcesTable.setWidthPercentage(100);
            resourcesTable.setWidths(new float[]{1, 2, 2});

            // Add each resource
            for(Ressources resource : resources) {
                // Icon cell (placeholder or actual icon)
                PdfPCell iconCell = new PdfPCell();
                iconCell.setPadding(10);
                iconCell.setBorder(Rectangle.BOTTOM);

                // Add resource icon if available, or use placeholder text
                if(resource.getImage() != null && !resource.getImage().isEmpty()) {
                    try {
                        Image icon = Image.getInstance(resource.getImage());
                        icon.scaleToFit(30, 30);
                        iconCell.addElement(icon);
                    } catch (Exception e) {
                        iconCell.addElement(new Paragraph("📚"));
                    }
                } else {
                    iconCell.addElement(new Paragraph("📚"));
                }

                // Name cell
                PdfPCell nameCell = new PdfPCell(new Paragraph(resource.getName()));
                nameCell.setPadding(10);
                nameCell.setBorder(Rectangle.BOTTOM);

                // Type cell
                PdfPCell typeCell = new PdfPCell(new Paragraph(resource.getType()));
                typeCell.setPadding(10);
                typeCell.setBorder(Rectangle.BOTTOM);

                resourcesTable.addCell(iconCell);
                resourcesTable.addCell(nameCell);
                resourcesTable.addCell(typeCell);
            }

            document.add(resourcesTable);
        }
    }

    private void addPosts(Document document, List<Posts> posts) throws DocumentException, IOException {
        if(posts != null && !posts.isEmpty()) {
            document.add(new Paragraph(" "));

            // Posts header
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE);
            PdfPTable postsHeaderTable = new PdfPTable(1);
            postsHeaderTable.setWidthPercentage(100);

            PdfPCell headerCell = new PdfPCell(new Paragraph("Posts", headerFont));
            headerCell.setBackgroundColor(new BaseColor(0, 102, 255)); // Blue background
            headerCell.setPadding(10);
            headerCell.setBorder(Rectangle.NO_BORDER);

            postsHeaderTable.addCell(headerCell);
            document.add(postsHeaderTable);

            // Add each post
            for(Posts post : posts) {
                PdfPTable postTable = new PdfPTable(1);
                postTable.setWidthPercentage(100);

                PdfPCell postCell = new PdfPCell();
                postCell.setPadding(10);
                postCell.setBorder(Rectangle.BOX);
                postCell.setBackgroundColor(new BaseColor(252, 252, 252)); // Light background for posts

                // Post title
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                Paragraph postTitle = new Paragraph(post.getTitle(), titleFont);

                // Type tag as a colored text
                Font typeFont = new Font(Font.FontFamily.HELVETICA, 10);
                typeFont.setColor(new BaseColor(24, 119, 242)); // Facebook blue
                Paragraph typeTag = new Paragraph(post.getType(), typeFont);

                // Post date
                Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                Paragraph postDate = new Paragraph("Posted on " + post.getCreated_at(), dateFont);

                // Post content
                Font contentFont = new Font(Font.FontFamily.HELVETICA, 12);
                Paragraph postContent = new Paragraph(post.getDescription(), contentFont);

                // Add post header content
                postCell.addElement(postTitle);
                postCell.addElement(typeTag);
                postCell.addElement(postDate);
                postCell.addElement(new Paragraph(" ")); // spacing
                postCell.addElement(postContent);

                // Add image if available
                if (post.getImages() != null && !post.getImages().isEmpty() && !post.getImages().equals("null")) {
                    try {
                        // Clean up the path - remove brackets and quotes if present
                        String imagePath = post.getImages();
                        if (imagePath != null) {
                            imagePath = imagePath.replaceAll("\\[\"", "").replaceAll("\"\\]", "");
                        }

                        System.out.println("Trying to load image: " + imagePath);

                        // Image loading with fallbacks
                        Image postImage = null;
                        boolean imageLoaded = false;

                        // Try 1: Direct file path loading
                        try {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists() && imageFile.isFile()) {
                                //System.out.println("Found image file on disk: " + imageFile.getAbsolutePath());
                                postImage = Image.getInstance(imageFile.getAbsolutePath());
                                imageLoaded = true;
                            }
                        } catch (Exception e) {
                            System.out.println("Failed to load as direct file: " + e.getMessage());
                        }

                        // Try 2: Resource path loading
                        if (!imageLoaded) {
                            try {
                                String resourcePath = "/images/posts/" + imagePath;
                                //System.out.println("Trying resource path: " + resourcePath);
                                InputStream resourceStream = getClass().getResourceAsStream(resourcePath);

                                if (resourceStream != null) {
                                    postImage = Image.getInstance(IOUtils.toByteArray(resourceStream));
                                    imageLoaded = true;
                                    System.out.println("Loaded from resources successfully");
                                } else {
                                    System.out.println("Resource not found: " + resourcePath);
                                }
                            } catch (Exception e) {
                                System.out.println("Failed to load from resources: " + e.getMessage());
                            }
                        }


                        if (imageLoaded && postImage != null) {
                            // Scale image to fit within the page width
                            float maxWidth = document.getPageSize().getWidth() - postCell.getPaddingLeft() - postCell.getPaddingRight() - 40;
                            postImage.scaleToFit(maxWidth, 250);
                            postImage.setAlignment(Element.ALIGN_CENTER);

                            // Add some spacing
                            postCell.addElement(new Paragraph(" "));
                            postCell.addElement(postImage);
                        } else {
                            throw new Exception("Could not locate image file");
                        }
                    } catch (Exception e) {
                        // image failed to load+placeholder text
                        Font placeholderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC, BaseColor.GRAY);
                        Paragraph imagePlaceholder = new Paragraph("Image could not be loaded: " + post.getImages(), placeholderFont);
                        imagePlaceholder.setAlignment(Element.ALIGN_CENTER);
                        postCell.addElement(new Paragraph(" "));
                        postCell.addElement(imagePlaceholder);
                    }
                }

                // Add status indicator
                Font statusFont = new Font(Font.FontFamily.HELVETICA, 10);
                BaseColor statusColor = "Active".equals(post.getStatus()) ?
                        new BaseColor(66, 183, 42) :
                        BaseColor.GRAY;
                statusFont.setColor(statusColor);

                Paragraph statusText = new Paragraph("Status: " + post.getStatus(), statusFont);
                statusText.setAlignment(Element.ALIGN_RIGHT);
                postCell.addElement(new Paragraph(" "));
                postCell.addElement(statusText);

                postTable.addCell(postCell);
                document.add(postTable);
                document.add(new Paragraph(" "));
            }
        }
    }

}