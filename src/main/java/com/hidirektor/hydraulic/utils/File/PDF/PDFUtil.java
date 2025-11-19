package com.hidirektor.hydraulic.utils.File.PDF;

import com.hidirektor.hydraulic.Launcher;
import com.hidirektor.hydraulic.controllers.pages.calculation.ClassicController;
import com.hidirektor.hydraulic.controllers.pages.calculation.PowerPackController;
import com.hidirektor.hydraulic.utils.System.SystemDefaults;
import com.hidirektor.hydraulic.utils.Utils;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.AnchorPane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PDFUtil {

    public static void pdfGenerator(String pngFilePath1,
                                    AnchorPane tankImage,
                                    AnchorPane schemeImage,
                                    String pdfFilePath,
                                    String girilenSiparisNumarasi,
                                    String kullanilacakKabin,
                                    String motorDegeri,
                                    String pompaDegeri,
                                    String unitType, boolean isKlasik) {
        try {
            String ExPDFFilePath = SystemDefaults.userDataPDFFolderPath + girilenSiparisNumarasi + ".pdf";

            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(ExPDFFilePath));
            document.open();

            PdfContentByte contentByte = writer.getDirectContentUnder();
            BaseColor backgroundColor = new BaseColor(255, 255, 255);
            contentByte.setColorFill(backgroundColor);
            contentByte.rectangle(0, 0, document.getPageSize().getWidth(), document.getPageSize().getHeight());
            contentByte.fill();

            // İlk resmi ekle ve boyutunu ayarla (yükseklik küçültüldü)
            Image image1 = Image.getInstance(Objects.requireNonNull(Launcher.class.getResource(pngFilePath1)));
            float targetWidth1 = document.getPageSize().getWidth() * 0.8f;  // Genişliği %80'e ayarla
            float targetHeight1 = (image1.getHeight() / (float) image1.getWidth()) * targetWidth1 * 0.7f; // Yüksekliği %70'e küçült
            image1.scaleToFit(targetWidth1, targetHeight1);
            image1.setAlignment(Image.ALIGN_CENTER);
            document.add(image1);

            // Türkçe karakter destekleyen fontu yükle
            BaseFont baseFont = BaseFont.createFont(String.valueOf(Launcher.class.getResource("/assets/fonts/Quicksand-Medium.ttf")), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font unicodeFont = new Font(baseFont, 22, Font.BOLD);

            // Girilen Sipariş Numarasını ve metni ekle
            Paragraph paragraph = new Paragraph(girilenSiparisNumarasi + " Numaralı Sipariş", unicodeFont);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.setSpacingBefore(15);  // 15dp üst boşluk
            document.add(paragraph);

            addAnchorPaneToPDF(tankImage, document, "tankImage");

            if(schemeImage != null) {
                addAnchorPaneToPDF(schemeImage, document, "schemeImage");
            }

            // "HALİL" metnini sayfanın en altına yerleştir
            Paragraph halilParagraph = new Paragraph(kullanilacakKabin, unicodeFont);
            halilParagraph.setAlignment(Element.ALIGN_CENTER);
            halilParagraph.setSpacingBefore(20);  // 20dp boşluk
            document.add(halilParagraph);

            if(pdfFilePath != null) {
                if(isKlasik) {
                    PdfReader reader = new PdfReader(Objects.requireNonNull(Launcher.class.getResource(pdfFilePath)));

                    document.newPage();

                    PdfImportedPage importedPage = writer.getImportedPage(reader, 1);
                    PdfContentByte cb = writer.getDirectContent();

                    cb.addTemplate(importedPage, 0, 0);

                    cb.beginText();
                    BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    cb.setFontAndSize(bf, 6);

                    float xPosition = document.getPageSize().getWidth() - 110; // Sağ kenar boşluğu
                    float yPosition = document.getPageSize().getHeight() - 65; // Sayfanın üstünden 50 birim boşluk

                    cb.setTextMatrix(xPosition, yPosition);
                    cb.showText(pompaDegeri);

                    yPosition -= 13;
                    cb.setTextMatrix(xPosition, yPosition);
                    cb.showText(motorDegeri);

                    cb.endText();

                    document.close();
                    writer.close();
                    reader.close();
                } else {
                    String motorText = "AC Motor";
                    if(PowerPackController.secilenMotorTipi.contains("DC")) {
                        motorText = "DC Motor";
                    }

                    PdfReader reader = new PdfReader(Objects.requireNonNull(Launcher.class.getResource(pdfFilePath)));

                    document.newPage();

                    PdfImportedPage importedPage = writer.getImportedPage(reader, 1);
                    PdfContentByte cb = writer.getDirectContent();

                    cb.addTemplate(importedPage, 0, 0);

                    cb.beginText();
                    BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                    cb.setFontAndSize(bf, 7);

                    float xPosition = document.getPageSize().getWidth() - 110; // Sağ kenar boşluğu
                    float yPosition = document.getPageSize().getHeight() - 53; // Sayfanın üstünden 50 birim boşluk

                    cb.setTextMatrix(xPosition - 112, yPosition);
                    cb.showText(motorText);

                    cb.setTextMatrix(xPosition - 182, yPosition);
                    cb.showText(motorText);

                    cb.setTextMatrix(xPosition, yPosition);
                    cb.showText(motorDegeri);

                    yPosition -= 13;
                    cb.setTextMatrix(xPosition, yPosition);
                    cb.showText(pompaDegeri);

                    cb.endText();

                    document.close();
                    writer.close();
                    reader.close();
                }
            } else {
                document.close();
                writer.close();
            }

            System.out.println("PDF oluşturuldu.");
            JSONObject jsonObject = new JSONObject();

            if(ClassicController.hesaplamaBitti) {
                jsonObject.put("Ünite Tipi", ClassicController.secilenUniteTipi);
                jsonObject.put("Sipariş Numarası", ClassicController.girilenSiparisNumarasi);
                jsonObject.put("Motor", ClassicController.secilenMotor);
                jsonObject.put("Soğutma", ClassicController.secilenSogutmaDurumu);
                jsonObject.put("Hidrolik Kilit", ClassicController.secilenHidrolikKilitDurumu);
                jsonObject.put("Pompa", ClassicController.secilenPompa);
                jsonObject.put("Gerekli Yağ Miktarı", ClassicController.girilenTankKapasitesiMiktari);
                jsonObject.put("Kompanzasyon", ClassicController.kompanzasyonDurumu);
                jsonObject.put("Valf Tipi", ClassicController.secilenValfTipi);
                jsonObject.put("Kilit Motor", ClassicController.secilenKilitMotor);
                jsonObject.put("Kilit Pompa", ClassicController.secilenKilitPompa);
                jsonObject.put("Seçilen Kampana", ClassicController.secilenKampana);
                jsonObject.put("Seçilen Pompa Val", ClassicController.secilenPompaVal);
            } else if(PowerPackController.hesaplamaBitti) {
                jsonObject.put("Ünite Tipi", PowerPackController.secilenUniteTipi);
                jsonObject.put("Sipariş Numarası", PowerPackController.girilenSiparisNumarasi);
                jsonObject.put("Motor Voltaj", PowerPackController.secilenMotorTipi);
                jsonObject.put("Ünite Durumu", PowerPackController.uniteTipiDurumu);
                jsonObject.put("Motor Gücü", PowerPackController.secilenMotorGucu);
                jsonObject.put("Pompa", PowerPackController.secilenPompa);
                jsonObject.put("Tank Tipi", PowerPackController.secilenTankTipi);
                jsonObject.put("Tank Kapasitesi", PowerPackController.secilenTankKapasitesi);
                jsonObject.put("Özel Tank Ölçüleri (GxDxY)", PowerPackController.secilenOzelTankGenislik + "x" + PowerPackController.secilenOzelTankDerinlik + "x" + PowerPackController.secilenOzelTankYukseklik);
                jsonObject.put("Platform Tipi", PowerPackController.secilenPlatformTipi);
                jsonObject.put("1. Valf Tipi", PowerPackController.secilenBirinciValf);
                jsonObject.put("İniş Metodu", PowerPackController.secilenInisTipi);
                jsonObject.put("2. Valf Tipi", PowerPackController.secilenIkinciValf);
            }

            if(SystemDefaults.loggedInUser != null) {
                Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                        girilenSiparisNumarasi,
                        Utils.getCurrentUnixTime(),
                        unitType,
                        ExPDFFilePath,
                        null,
                        "no",
                        SystemDefaults.loggedInUser.getUserID(),
                        jsonObject);
            } else {
                Utils.createLocalUnitData(SystemDefaults.userLocalUnitDataFilePath,
                        girilenSiparisNumarasi,
                        Utils.getCurrentUnixTime(),
                        unitType,
                        ExPDFFilePath,
                        null,
                        "yes",
                        System.getProperty("user.name"),
                        jsonObject);
            }

            new File("processed_image.png").delete();

            // PDF otomatik açılmıyor - kullanıcı "Dosyada Göster" butonunu kullanabilir
            // if (Desktop.isDesktopSupported()) {
            //     try {
            //         File pdfFile = new File(ExPDFFilePath);
            //         Desktop.getDesktop().open(pdfFile);
            //     } catch (IOException e) {
            //         System.out.println(e.getMessage());
            //     }
            // }
        } catch (DocumentException | IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addAnchorPaneToPDF(AnchorPane calculationResultSection, Document document, String pngFilePath2) throws Exception {
        WritableImage snapshot = calculationResultSection.snapshot(new SnapshotParameters(), null);

        File pngFile = new File(pngFilePath2);
        ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", pngFile);

        Image image2 = Image.getInstance(pngFilePath2);

        float targetWidth2 = document.getPageSize().getWidth() * 0.8f;

        float targetHeight2 = (image2.getHeight() / (float) image2.getWidth()) * targetWidth2 * 0.55f;

        image2.scaleToFit(targetWidth2, targetHeight2);

        image2.setAlignment(Image.ALIGN_CENTER);
        image2.setSpacingBefore(10);

        document.add(image2);

        if (pngFile.exists() && pngFile.delete()) {
            System.out.println("Geçici PNG dosyası silindi: " + pngFilePath2);
        }
    }

    public static void cropImage(int startX, int startY, int width, int height, String fileName) {
        try {
            BufferedImage originalImage = ImageIO.read(new File("screenshot.png"));

            BufferedImage croppedImage = originalImage.getSubimage(startX, startY, width, height);

            ImageIO.write(croppedImage, "png", new File(fileName));
            System.out.println("Kırpılmış fotoğraf başarıyla kaydedildi: " + fileName);

            File originalFile = new File("screenshot.png");
            if (originalFile.delete()) {
                System.out.println("Eski fotoğraf başarıyla silindi: " + "screenshot.png");
            } else {
                System.out.println("Eski fotoğraf silinemedi: " + "screenshot.png");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void coords2Png(int startX, int startY, int width, int height, Button exportButton) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(new Rectangle2D(startX, startY, width, height));

        WritableImage screenshot = exportButton.getScene().snapshot(null);

        File outputFile = new File("screenshot.png");

        BufferedImage bufferedImage = convertToBufferedImage(screenshot);

        try {
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("Ekran görüntüsü başarıyla kaydedildi: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Ekran görüntüsü kaydedilirken bir hata oluştu: " + e.getMessage());
        }
    }

    private static BufferedImage convertToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        PixelReader pixelReader = writableImage.getPixelReader();
        WritablePixelFormat<IntBuffer> pixelFormat = WritablePixelFormat.getIntArgbInstance();

        int[] pixelData = new int[width * height];
        pixelReader.getPixels(0, 0, width, height, pixelFormat, pixelData, 0, width);

        bufferedImage.setRGB(0, 0, width, height, pixelData, 0, width);

        return bufferedImage;
    }

    public static BufferedImage convertWhiteToBlack(BufferedImage originalImage) {
        BufferedImage newImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb, true);

                if (color.getRed() > 200 && color.getGreen() > 200 && color.getBlue() > 200) {
                    newImage.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    newImage.setRGB(x, y, rgb);
                }
            }
        }
        return newImage;
    }

    public static void loadPDFPagesToImageViews(String pdfPath, ImageView schemePageOne, ImageView schemePageTwo) {
        try {
            // PDF dosyasını yükle
            PDDocument document = PDDocument.load(new File(pdfPath));

            // PDF Renderer oluştur
            PDFRenderer renderer = new PDFRenderer(document);

            // İlk sayfayı görüntü olarak al
            if (document.getNumberOfPages() > 0) {
                BufferedImage pageOneImage = renderer.renderImageWithDPI(0, 150); // 150 DPI çözünürlük
                schemePageOne.setImage(convertToJavaFXImage(pageOneImage));
            }

            // İkinci sayfayı görüntü olarak al
            if (document.getNumberOfPages() > 1) {
                BufferedImage pageTwoImage = renderer.renderImageWithDPI(1, 150);
                schemePageTwo.setImage(convertToJavaFXImage(pageTwoImage));
            }

            // PDF dosyasını kapat
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("PDF dosyasını işlerken bir hata oluştu: " + e.getMessage());
        }
    }

    // BufferedImage -> JavaFX Image dönüşümü
    private static WritableImage convertToJavaFXImage(BufferedImage bufferedImage) {
        javafx.embed.swing.SwingFXUtils.fromFXImage(
                javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null), null
        );
        return javafx.embed.swing.SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    // Dosya gezgininde/finder'da aç
    public static void openFileInExplorer(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.err.println("Dosya bulunamadı: " + filePath);
                return;
            }
            
            if (os.contains("win")) {
                // Windows: explorer /select,"dosya_yolu"
                String command = "explorer /select,\"" + file.getAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
            } else if (os.contains("mac")) {
                // Mac: open -R "dosya_yolu"
                String command = "open -R \"" + file.getAbsolutePath() + "\"";
                Runtime.getRuntime().exec(command);
            } else {
                // Linux: xdg-open ile klasörü aç
                Path parentPath = Paths.get(file.getAbsolutePath()).getParent();
                if (parentPath != null) {
                    String command = "xdg-open \"" + parentPath.toString() + "\"";
                    Runtime.getRuntime().exec(command);
                }
            }
        } catch (IOException e) {
            System.err.println("Dosya gezgininde açılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}