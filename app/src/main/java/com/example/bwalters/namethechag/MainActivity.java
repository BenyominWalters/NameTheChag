package com.example.bwalters.namethechag;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    ArrayList<String> images = new ArrayList<>();
    ArrayList<String> imageDescriptions = new ArrayList<>();
    ArrayList<String> answers = new ArrayList<>();

    int correctLocation;



    // Open internet connection on a separate thread and download html from target page.

    public class DownloadTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }
            catch(Exception e) {

                e.printStackTrace();
                return "Failed to connect to url!";

            }


        }

    }


    // Use regex to find all item image thumbnails and descriptions in html from page.
    // Make a list of the links for the images in the images ArrayList,
    // and add their descriptions to the imageDescriptions ArrayList.
    public String findImages (String html) {

        String imageLink = null;

        images.clear();
        imageDescriptions.clear();

        Pattern p = Pattern.compile("<img src=\"(.*?)\"");
        Matcher m = p.matcher(html);

        while (m.find()) {

            images.add(m.group(1));
            imageLink = images.get(0);

        }


        p = Pattern.compile("\"itemDesc\">(.*?)</p>");
        m = p.matcher(html);

        while (m.find()) {

            imageDescriptions.add(m.group(1));

        }

        return imageLink;

    }

    // Download a selected image on a separate thread.
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;

        }
    }

    // Downloads and renders a random image from list of image links.
    // Save the ArrayList position of the corresponding image description.
    // Save the correct answer position to a the variable randomImage, and generate three random positions
    // Fill the buttons with the four answers
    public void downloadImage (View view) {

        ImageDownloader task = new ImageDownloader();

        Random rand = new Random();
        int randomImage = rand.nextInt(images.size());
        int imageDescription = randomImage;

        Bitmap myImage;

        try {

           myImage = task.execute(images.get(randomImage)).get();

           imageView.setImageBitmap(myImage);

           answers.clear();

           correctLocation = rand.nextInt(4);

           int incorrectAnswer;

           for (int i=0; i < 4; i++) {

               if (i == correctLocation) {

                   answers.add(imageDescriptions.get(imageDescription));

               } else {

                   incorrectAnswer = rand.nextInt(imageDescriptions.size());

                   while (incorrectAnswer == correctLocation) {

                       incorrectAnswer = rand.nextInt(imageDescriptions.size());

                   }

                answers.add(imageDescriptions.get(incorrectAnswer));

               }

           }

           button1.setText(answers.get(0));
           button2.setText(answers.get(1));
           button3.setText(answers.get(2));
           button4.setText(answers.get(3));

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    // Check if selected correct answer, and Toast correct or wrong.
    // Call new random image.
    public void chooseAnswer (View view) {
        Log.i("Tag", view.getTag().toString());
        Log.i("correctLocation", Integer.toString(correctLocation));

        if (view.getTag().toString().equals(Integer.toString(correctLocation))) {

            Toast correctToast = Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG);
            correctToast.show();

        } else {

            Toast wrongToast = Toast.makeText(getApplicationContext(), "Wrong!", Toast.LENGTH_LONG);
            wrongToast.show();

        }

        downloadImage(imageView);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);


        DownloadTask getHtml = new DownloadTask();
        String result = null;


        try {
            result = getHtml.execute("https://www.waldereducation.org/resource/clipart/?F_ValueIds=1209%2c382&F_Sort=2").get();

        } catch (InterruptedException e) {

            e.printStackTrace();

        } catch (ExecutionException e) {

            e.printStackTrace();

        }

        findImages(result);
        //downloadImage(imageView);

        Log.i("Contents of URL:", result);
        Log.i("Image list length", Integer.toString(images.size()));
        Log.i("Image description list length", Integer.toString(imageDescriptions.size()));

        for(int i=0; i < images.size(); i++ ) {
          Log.i("My Array of Links:", images.get(i));
          //Log.i("My Array of Descriptions:", imageDescriptions.get(i));
        }


    }
}
