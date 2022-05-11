package com.johnny.tutu_test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;

import com.johnny.tutu_test.model.Pokemon;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FetchPokemonImages {
    private final MainActivityViewModel viewModel;
    private final HashMap<Integer, MutableLiveData<Boolean>> imagesLoadingMap;
    private final Executor executor = Executors.newFixedThreadPool(2);
    private static FetchPokemonImages thread;

    public FetchPokemonImages(@NonNull MainActivityViewModel viewModel) {
        this.viewModel = viewModel;
        thread = this;

        imagesLoadingMap = new HashMap<>();
        for (Pokemon pokemon : viewModel.getPokemonList())
            imagesLoadingMap.put(pokemon.getPokemonId(), new MutableLiveData<>(false));
    }

    @Nullable
    public static FetchPokemonImages get() {
        return thread;
    }

    public HashMap<Integer, MutableLiveData<Boolean>> getImagesLoadingMap() {
        return imagesLoadingMap;
    }

    boolean loadImage(int pokemonId) {
        return false;
    }

    public void loadImage(int pokemonId, URL url) {
        executor.execute(() -> {
            if (executeLoading(pokemonId, url))
                Objects.requireNonNull(imagesLoadingMap.get(pokemonId)).postValue(true);
        });
    }

    @WorkerThread
    private boolean executeLoading(int pokemonId, URL url) {
        Log.d("TAG", "Got a request to load image for pokemonId: " + pokemonId);
        try {
            String appDirPath = viewModel.getApplication().getFilesDir().getAbsolutePath();
            loadImageInFile(url, appDirPath + "/images/thumbnails", "pok_id_" + pokemonId + "_thumb.png", true);
            loadImageInFile(url, appDirPath + "/images/orig", "pok_id_" + pokemonId + ".png", false);
            return true;
        }
        catch (IOException e) {
            Log.d("TAG", "failed while loading image for pokemon with id: " + pokemonId);
            e.printStackTrace();
            return false;
        }
    }

    @WorkerThread
    private void loadImageInFile(@NonNull URL url, String folderName, String fileName, boolean compress) throws IOException {
        File file = new File(folderName, fileName);
        if (file.exists())
            return;
        Bitmap image = loadImageFromUrl(url, compress);
        Objects.requireNonNull(file.getParentFile()).mkdirs();
        file.createNewFile();
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        stream.flush();
        stream.close();
    }

    @NonNull
    @WorkerThread
    private Bitmap loadImageFromUrl(@NonNull URL url, boolean compress) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = httpURLConnection.getInputStream();
        Bitmap image;
        if (compress) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            image = BitmapFactory.decodeStream(inputStream, null, options);
        }
        else
            image = BitmapFactory.decodeStream(inputStream);
        if (image == null)
            throw new IOException("cannot get image for pokemon with url: " + url);
        inputStream.close();
        httpURLConnection.disconnect();
        return image;
    }
}