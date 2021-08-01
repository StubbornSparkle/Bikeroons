package exportkit.xd;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class qr_activity extends Activity {

    private ImageView qr;
    private EditText text;
    Bitmap bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrpopup);


        qr = (ImageView) findViewById(R.id.qr);
        text = (EditText) findViewById(R.id.text);
        text.setText(getAlphaNumericString(7));

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(text.getText().toString(), BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qr.setImageBitmap(bitmap);


           // imgToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    static String getAlphaNumericString(int n) {
        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }
        return sb.toString();
    }


    public class BarcodeEncoder {
        private static final int WHITE = 0xFFFFFFFF;
        private static final int BLACK = 0xFF000000;

        public BarcodeEncoder() {
        }

        public Bitmap createBitmap(BitMatrix matrix) {
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        }

        public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
            try {
                return new MultiFormatWriter().encode(contents, format, width, height);
            } catch (WriterException e) {
                throw e;
            } catch (Exception e) {
                // ZXing sometimes throws an IllegalArgumentException
                throw new WriterException(e);
            }
        }

        public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
            try {
                return new MultiFormatWriter().encode(contents, format, width, height, hints);
            } catch (WriterException e) {
                throw e;
            } catch (Exception e) {
                throw new WriterException(e);
            }
        }

        public Bitmap encodeBitmap(String contents, BarcodeFormat format, int width, int height) throws WriterException {
            return createBitmap(encode(contents, format, width, height));
        }

        public Bitmap encodeBitmap(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
            return createBitmap(encode(contents, format, width, height, hints));
        }
    }


}
