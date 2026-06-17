package net.orderzone.idcard.service;

import net.orderzone.idcard.model.BarcodeType;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Service
public class BarcodeService {

    public byte[] generateBarcode(String data, BarcodeType type) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (type == BarcodeType.EAN_13) {
            String eanData = data.replaceAll("[^0-9]", "");
            while (eanData.length() < 12) eanData = "0" + eanData;
            if (eanData.length() > 12) eanData = eanData.substring(eanData.length() - 12);
            EAN13Bean bean = new EAN13Bean();
            bean.setModuleWidth(2);
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    baos, "image/png", 150, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            bean.generateBarcode(canvas, eanData);
            canvas.finish();
        } else {
            Code128Bean bean = new Code128Bean();
            bean.setModuleWidth(2);
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                    baos, "image/png", 150, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            bean.generateBarcode(canvas, data);
            canvas.finish();
        }
        return baos.toByteArray();
    }
}