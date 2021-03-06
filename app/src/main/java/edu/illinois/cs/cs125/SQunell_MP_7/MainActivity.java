package edu.illinois.cs.cs125.SQunell_MP_7;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.method.MovementMethod;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


/**
 * Main class for MP.
 */
public final class MainActivity extends AppCompatActivity {

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", BuildConfig.API_KEY1);

    /**
     * Opens up the camera to take a picture
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ProgressDialog detectionProgressDialog;


    public static final int PICK_IMAGE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    Bitmap imageBitmap;

   //Sets the image view to the photo that was just taken
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "On Activity Result Ran");


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            pic.setImageBitmap(imageBitmap);
        }


      /*  if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Log.d(TAG, "Selected image is about to be placed.");
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            pic.setImageBitmap(imageBitmap);
        } */

     /*   if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            // Let's read picked image data - its URI
           Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            // Now we need to set the GUI ImageView data with data read from the picked file.
            pic.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        } */



    }








    ImageView pic;
    Button submit;
    Button photo;
    TextView recs;
    Button newfood;
    Button redo;
    Button restaurant;
    EditText address;

    double anger;
    double contempt;
    double disgust;
    double fear;
    double happiness;
    double neutral;
    double sadness;
    double surprise;
    String currentaddress = "201 N Goodwin Ave, Urbana, IL 61801";

    //Temporary variable- Just used to switch between two preset recommended food options
    int counter;


    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "MP_7:";
    /** current emotion. */
    String emotion;
    /**current food. */
    String food = "PAIN";


    /** Request queue for our API requests. */
    private static RequestQueue requestQueue;

    /**
     * Run when this activity comes to the foreground.
     *
     * @param savedInstanceState unused
     */

    private static String latest;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the queue for our API requests
        requestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);



        detectionProgressDialog = new ProgressDialog(this);

        newfood = findViewById(R.id.Refood);
        newfood.setVisibility(View.INVISIBLE);
        newfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emoto(emotion);

            }
        });


        restaurant = findViewById(R.id.Explore);
        restaurant.setVisibility(View.INVISIBLE);
        restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Explore restaurant button clicked");


                //Converts all spaces that the user entered to pluses so the API can use it
                String input = address.getText().toString();

                char[] newInput = input.toCharArray();

                for (int i = 0; i < newInput.length; i++){
                    if (newInput[i] == ' '){
                        newInput[i] = '+';
                    }
                }

                String finalInput = String.valueOf(newInput);

                foodAPI(finalInput);

            }
        });

        redo = findViewById(R.id.Redo);
        redo.setVisibility(View.INVISIBLE);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "redo photo button clicked");
                //RETURN TO STARTING STATE/VISIBILITY

                newfood.setVisibility(View.INVISIBLE);
                restaurant.setVisibility(View.INVISIBLE);
                redo.setVisibility(View.INVISIBLE);
                recs.setVisibility(View.INVISIBLE);
                photo.setVisibility(View.VISIBLE);
                submit.setVisibility(View.INVISIBLE);
                pic.setVisibility(View.INVISIBLE);
                address.setVisibility(View.INVISIBLE);
            }
        });

        recs = findViewById(R.id.Recommend);
        recs.setVisibility(View.INVISIBLE);
        recs.setMovementMethod(new ScrollingMovementMethod());

        photo = findViewById(R.id.Photo);
        photo.setVisibility(View.VISIBLE);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "take photo button clicked");
                //TAKE PHOTO METHOD
                dispatchTakePictureIntent();
                recs.setVisibility(View.INVISIBLE);
                pic.setVisibility(View.VISIBLE);
                submit.setVisibility(View.VISIBLE);

            }
        });
        address = findViewById(R.id.Address);
        address.setVisibility(View.INVISIBLE);

        submit = findViewById(R.id.Submit);
        submit.setVisibility(View.INVISIBLE);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Submit photo button clicked");
                //ANALYZE PHOTO METHOD

                detectAndFrame(imageBitmap);

            }
        });



        pic = findViewById(R.id.Image);
        pic.setVisibility(View.INVISIBLE);






    }

    /**
     * Run when this activity is no longer visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }
    /**parse data from face api. */
    // not tested
    /**void faceParse(final String json) {
        JsonParser parser = new JsonParser();
        try {
            JsonArray peepz = parser.parse(json).getAsJsonArray();
            if (peepz.size() == 0 ) {
                recs.setText("Sorry, we need a clear photo of a human face.");
                return;
            } else if (peepz.size() > 1) {
                recs.setText("Please submit a photo of just one face.");
                return;
            } else {
                JsonObject emotions = peepz.get(0).getAsJsonObject().getAsJsonObject("faceAttributes")
                        .getAsJsonObject("emotion");
                double anger = emotions.get("anger").getAsDouble();
                double contempt = emotions.get("contempt").getAsDouble();
                double disgust = emotions.get("disgust").getAsDouble();
                double fear = emotions.get("fear").getAsDouble();
                double happiness = emotions.get("happiness").getAsDouble();
                double neutral = emotions.get("neutral").getAsDouble();
                double sadness = emotions.get("sadness").getAsDouble();
                double surprise = emotions.get("surprise").getAsDouble();
                double biggest = Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(Math.max(anger,contempt),disgust),fear),happiness),neutral),sadness),surprise);
                String finalemot;
                if (anger == biggest) {
                    finalemot = "anger";
                    //Intentionally merging contempt and disgust
                } else if (contempt == biggest) {
                    finalemot = "disgust";
                } else if (disgust == biggest) {
                    finalemot = "disgust";
                } else if (fear == biggest) {
                    finalemot = "fear";
                } else if (happiness == biggest) {
                    finalemot = "happiness";
                } else if (neutral == biggest) {
                    finalemot = "neutral";
                } else if (sadness == biggest) {
                    finalemot = "sadness";
                } else {
                    //Intentionally merging fear and surprise
                    finalemot = "fear";
                }
                emoto(finalemot);
                //not sure why these visibilities are flagging checkstyle
                newfood.setVisibility(View.VISIBLE);
                restaurant.setVisibility(View.VISIBLE);
                redo.setVisibility(View.VISIBLE);
                recs.setVisibility(View.VISIBLE);
                importz.setVisibility(View.INVISIBLE);
                photo.setVisibility(View.INVISIBLE);
                submit.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            recs.setText("We really didn't like that image. Git gud.");
        }
    }
**/
    /** set emotion. needs actual foods */
    void emoto(final String emotionk) {
        String newfood = new String(food);
        emotion = emotionk;
        Random index = new Random();
        if (emotionk.equals("anger") || emotionk.equals("disgust") || emotionk.equals("contempt")) {
            String[] foodz = {"ice cream", "cookies", "cake", "pie", "chocolate"};
            while(newfood.equals(food)) {
                newfood = foodz[index.nextInt(foodz.length)];
            }
            recs.setText("We detected anger. You could use something sweet to calm you down. " +
                    "Perhaps some " + newfood+" will quell your rage.");
        } else if(emotionk.equals("fear") || emotionk.equals("surprise")) {
            String[] foodz = {"pizza", "tacos", "pasta", "bread", "chicken", "rice"};
            while(newfood.equals(food)) {
                newfood = foodz[index.nextInt(foodz.length)];
            }
            recs.setText("We detected fear. Something simple could help you relax. " +
                    "Chill out for a bit with some " + newfood+"!");

        } else if(emotionk.equals("neutral")) {
            String[] foodz = {"Chinese", "Italian", "Korean", "Indian", "Greek", "Mexican", "Polish"};
            while(newfood.equals(food)) {
                newfood = foodz[index.nextInt(foodz.length)];
            }
            recs.setText("We didn't detect much emotion. Something new and exciting could spice up your day. " +
                    "We recommend " + newfood+" cuisine.");
        } else if(emotionk.equals("happiness")) {
            String[] foodz = {"sandwich", "burger", "salad", "hot dog"};
            while(newfood.equals(food)) {
                newfood = foodz[index.nextInt(foodz.length)];
            }
            recs.setText("We detected happiness. If it ain't broke, don't fix it! " +
                    "A classic " + newfood+" certainly won't hurt your mood.");
        } else {
            String[] foodz = {"tea", "coffee", "soup", };
            while(newfood.equals(food)) {
                newfood = foodz[index.nextInt(foodz.length)];
            }
            recs.setText("We detected sadness. Something soothing would perk you up! " +
                    "We bet you'd love some " + newfood+"!");
        }
        food = newfood;
    }

    /**
     * Make a call to the face API. NOT COMPLETE
     */
    /*void photoAPI() {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://westcentralus.api.cognitive.microsoft.com/face/v1.0"
                            + BuildConfig.API_KEY1,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                Log.d(TAG, response.toString(2));
                                latest = response.toString();
                                faceParse(latest);
                            } catch (JSONException ignored) { }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(final VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
    /** parses restaurant data and updates text. */
    void restauranter(final String json) {
        JsonParser parser = new JsonParser();
        JsonArray restaurants = parser.parse(json).getAsJsonObject().getAsJsonArray("restaurants");
        String reco = "Here are some suggestions:\n";
        for (int i = 0; i < restaurants.size(); i++) {
            JsonObject thisrest = restaurants.get(i).getAsJsonObject();
            String name = thisrest.get("name").getAsString();
            String address = thisrest.get("streetAddress").getAsString();
            reco += "Name: " + name + "\n";
            reco += "Address: " + address + "\n\n";
            if (i == 4) {
                break;
            }
        }
        recs.setText(reco);
    }

    private static Bitmap drawFaceRectanglesOnBitmap(Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        int stokeWidth = 3;
        paint.setStrokeWidth(stokeWidth);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

    // Detect faces by uploading face images
    // Frame faces after detection

    private void detectAndFrame(final Bitmap imageBitmap)
    {
        Log.d(TAG, "DETECT AND FRAME STARTING");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    new FaceServiceClient.FaceAttributeType[]{FaceServiceClient.FaceAttributeType.Emotion}           // returnFaceAttributes: a string like "age, gender"
                            );
                            if (result == null)
                            {
                                publishProgress("Detection Finished. Nothing detected");
                                Log.d(TAG, "NOTHING DETECTED");
                                return null;
                            }
                            publishProgress(
                                    String.format("Detection Finished. %d face(s) detected",
                                            result.length));
                                Log.d(TAG, "DETECTION WORKED");
                                Log.d(TAG, "The number of faces is " + String.valueOf(result.length));

                                if (result.length == 0) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"No face was found. Please try again.",Toast.LENGTH_LONG).show();
                                           // recs.setVisibility(View.VISIBLE);
                                            //recs.setText("No face detected. Please try again.");
                                        }
                                    });


                                }

                    /*            Log.d(TAG, "ABOUT TO TOAST");
                                Looper.prepare();
                            Toast.makeText(getApplicationContext(),"No faces found. Please try again.",Toast.LENGTH_LONG).show();
                            Log.d(TAG, "DONE WITH TOASTING"); */





                                Log.d(TAG, "Anger is " + String.valueOf(result[0].faceAttributes.emotion.anger));
                                Log.d(TAG, "Contempt is " + String.valueOf(result[0].faceAttributes.emotion.contempt));
                                Log.d(TAG, "Disgust is " + String.valueOf(result[0].faceAttributes.emotion.disgust));
                                Log.d(TAG, "Fear is " + String.valueOf(result[0].faceAttributes.emotion.fear));
                                Log.d(TAG, "Happiness is " + String.valueOf(result[0].faceAttributes.emotion.happiness));
                            Log.d(TAG, "Neutral is " + String.valueOf(result[0].faceAttributes.emotion.neutral));
                            Log.d(TAG, "Sadness is " + String.valueOf(result[0].faceAttributes.emotion.sadness));
                            Log.d(TAG, "Surprise is " + String.valueOf(result[0].faceAttributes.emotion.surprise));
                            anger = result[0].faceAttributes.emotion.anger;
                            contempt = result[0].faceAttributes.emotion.contempt;
                            disgust = result[0].faceAttributes.emotion.disgust;
                            fear = result[0].faceAttributes.emotion.fear;
                            happiness = result[0].faceAttributes.emotion.happiness;
                            neutral = result[0].faceAttributes.emotion.neutral;
                            sadness = result[0].faceAttributes.emotion.sadness;
                            surprise = result[0].faceAttributes.emotion.surprise;

                            //Decides which emotion is present

                            String currentEmotion = "anger";
                            double currentEmotionValue = anger;

                            if (contempt > currentEmotionValue) {
                                currentEmotion = "contempt";
                                currentEmotionValue = contempt;
                            }

                            if (disgust > currentEmotionValue) {
                                currentEmotion = "disgust";
                                currentEmotionValue = disgust;
                            }

                            if (fear > currentEmotionValue) {
                                currentEmotion = "fear";
                                currentEmotionValue = fear;
                            }

                            if (happiness > currentEmotionValue) {
                                currentEmotion = "happiness";
                                currentEmotionValue = happiness;
                            }

                            if (neutral > currentEmotionValue) {
                                currentEmotion = "neutral";
                                currentEmotionValue = neutral;
                            }

                            if (sadness > currentEmotionValue) {
                                currentEmotion = "sadness";
                                currentEmotionValue = sadness;
                            }

                            if (surprise > currentEmotionValue) {
                                currentEmotion = "surprise";
                                currentEmotionValue = surprise;
                            }
                            Log.d(TAG,currentEmotion);
                            final String finalemo = currentEmotion;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    emotion = finalemo;
                                    Log.d(TAG, emotion);
                                    emoto(emotion);
                                    submit.setVisibility(View.INVISIBLE);
                                    recs.setVisibility(View.VISIBLE);
                                    newfood.setVisibility(View.VISIBLE);
                                    restaurant.setVisibility(View.VISIBLE);
                                    redo.setVisibility(View.VISIBLE);
                                    photo.setVisibility(View.INVISIBLE);
                                    address.setVisibility(View.VISIBLE);
                                }
                            });

                            return result;
                        } catch (Exception e) {

                            Log.d(TAG, "DETECTION RAN BUT DIDN'T WORK" + e);
                            publishProgress("Detection failed");
                            return null;
                        }
                    }
                    @Override
                    protected void onPreExecute() {
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
                        detectionProgressDialog.dismiss();
                        if (result == null) return;
                        pic.setImageBitmap(drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
                    }
                };
        detectTask.execute(inputStream);
    }



    /** Call eatstreet API. NOT COMPLETE **/
    void foodAPI(String userInput) {
        try {
            //yoink current text, format, and add in place of current addresss
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://api.eatstreet.com/publicapi/v1/restaurant/search?method=both"
                            + "&access-token="+BuildConfig.API_KEY2+"&search="
                            +food+"&street-address=" + userInput,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                Log.d(TAG, response.toString(2));
                                latest = response.toString();
                                restauranter(latest);
                            } catch (JSONException ignored) { }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
