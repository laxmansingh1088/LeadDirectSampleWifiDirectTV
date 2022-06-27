package com.example.leadp2pdirect.servers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.leadp2pdirect.P2PCallBacks;
import com.example.leadp2pdirect.utils.Utils;
import com.example.leadp2pdirect.helpers.callbacks.Callback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmitryMarinin on 23.03.2018.
 */

public class FileServerAsyncTask extends AsyncTask<Void, FileDownloadUploadProgresssModel, Void> {

    private Context context;
    private ServerSocket serverSocket;
    private Socket client;
    private Long fileSize;
    private Long fileSizeOriginal;
    public File[] receivedFiles;
    private Callback referenceCallback;
    private P2PCallBacks p2PCallBacks;
    private ArrayList<FileModel> receivedFilesPathList = new ArrayList<>();
    private String timeTakenbyFile = "";


    public FileServerAsyncTask(Context contextWeakReference, ServerSocket reference, File[] receivedFiles, Callback callback, P2PCallBacks p2PCallBacks) {
        this.context = contextWeakReference;
        this.serverSocket = reference;
        this.receivedFiles = receivedFiles;
        this.referenceCallback = callback;
        this.p2PCallBacks = p2PCallBacks;
    }

    private ContentResolver getContentResolverInstance() {
        return context.getContentResolver();
    }

    private void receiveData() {
        Log.d("Receiver", "receiveData()");
        byte buf[] = new byte[8192];
        int len = 0;
        try {
            Log.d("Receiver", "Server Listening");
            client = serverSocket.accept();
            Log.d("Receiver", "Server Connected");
            if (isCancelled()) return;

            InputStream inputStream = client.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            File file = new File(FileHelper.INSTANCE.getRootDirectoryPath(context));
            if (file != null && file.exists()) {
                FileHelper.INSTANCE.deleteDir(file);
            }
            //Get filemodel.........
            ArrayList<FileModel> fileModelArrayList = (ArrayList<FileModel>) objectInputStream.readObject();
            for (int i = 0; i < fileModelArrayList.size(); i++) {
                FileModel fileModel = fileModelArrayList.get(i);
                String fileName = fileModel.getFileName();
                fileSize = fileModel.getFileLength();
                fileSizeOriginal = fileSize;
                file = new File(FileHelper.INSTANCE.getRootDirectoryPath(context) + "/" + fileName);
                Log.d("Receiver", file.getPath());
                File dir = file.getParentFile();
                // Utils.INSTANCE.deleteDir(dir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (file.exists()) file.delete();

                if (file.createNewFile()) {
                    Log.d("Receiver", "File Created");

                } else Log.d("Receiver", "File Not Created");

                OutputStream outputStream = new FileOutputStream(file);
                //customObject need for progress update
                FileDownloadUploadProgresssModel progress = new FileDownloadUploadProgresssModel();
                progress.setFileName(fileName);
                progress.setDataIncrement(0);
                progress.setTotalProgress(0);
                progress.setSendingOrReceiving(FileDownloadUploadProgresssModel.SendingOrReceiving.Receiving.name());
                progress.setFileLength(fileSize);
                try {
                    Log.d("whileloopp", "FileServerAsyncTask.java -- Receiving Started===================");
                    while (fileSize > 0 &&
                            (len = objectInputStream.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                        outputStream.write(buf, 0, len);
                        outputStream.flush();

                        fileSize -= len;

                        progress.setDataIncrement((long) len);
                        if (((int) (progress.getTotalProgress() * 100 / fileSizeOriginal)) ==
                                ((int) ((progress.getTotalProgress() + progress.getDataIncrement()) * 100 / fileSizeOriginal))) {
                            progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                            continue;
                        }

                        progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                        publishProgress(progress);
                        if (this.isCancelled()) return;

                    }

                    fileModel.setAbsoluteFilePath(file.getAbsolutePath());
                    receivedFilesPathList.add(fileModel);

                    Log.d("whileloopp", "FileServerAsyncTask.java -- Receiving Finished===================");
                } catch (Exception e) {
                    Log.d("Receiver", "oops");
                    e.printStackTrace();
                }
                outputStream.flush();
                outputStream.close();
            }
            objectInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendData(ArrayList<Uri> uris) {
        int len = 0;
        byte buf[] = new byte[8192];

        ContentResolver cr = getContentResolverInstance();
        try {
            client = serverSocket.accept();

            InputStream inputStreamSocket = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStreamSocket);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ArrayList<FileModel> fileModelArrayList = FileHelper.INSTANCE.getFileModelsListFromUris(uris, cr);
            objectOutputStream.writeObject(fileModelArrayList);
            objectOutputStream.flush();

            for (int i = 0; i < uris.size(); i++) {
                InputStream inputStream = cr.openInputStream(uris.get(i));

                FileModel fileModel = fileModelArrayList.get(i);
                FileDownloadUploadProgresssModel progress = new FileDownloadUploadProgresssModel();
                progress.setFileName(fileModel.getFileName());
                progress.setDataIncrement(0);
                progress.setTotalProgress(0);
                progress.setSendingOrReceiving(FileDownloadUploadProgresssModel.SendingOrReceiving.Sending.name());
                progress.setFileLength(fileModel.getFileLength());

                long startTime = System.currentTimeMillis();
                while ((len = inputStream.read(buf)) != -1) {
                    objectOutputStream.write(buf, 0, len);
                    objectOutputStream.flush();
                    progress.setDataIncrement((long) len);
                    if (((int) (progress.getTotalProgress() * 100 / fileModel.getFileLength())) ==
                            ((int) ((progress.getTotalProgress() + progress.getDataIncrement()) * 100 / fileModel.getFileLength()))) {
                        progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                        continue;
                    }
                    progress.setTotalProgress(progress.getTotalProgress() + progress.getDataIncrement());
                    publishProgress(progress);
                }
                inputStream.close();
                Log.d("TRANSFER", "Writing Data Final   -" + len);

                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;

                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
                long seconds = (TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60);
                timeTakenbyFile = "FileSize:- " + Utils.INSTANCE.humanReadableByteCountSI(fileModel.getFileLength()) + " &&  Time Taken:- " + minutes + " min : " + seconds + " sec";
                Log.d("Time--elapsed:->", timeTakenbyFile);
            }
            outputStream.close();
            objectOutputStream.close();

        } catch (Exception e) {
            Log.d("Data Transfer", e.toString());
            e.printStackTrace();
        } finally {
        }
    }


    @Override
    protected Void doInBackground(Void... params) {
        Log.d("Receiver", "doInBackground");
        receiveData();
        return null;
    }

    @Override
    protected void onProgressUpdate(FileDownloadUploadProgresssModel... values) {
        FileDownloadUploadProgresssModel fileDownloadProgresssModel = values[0];
        // Total progress
        int progress = (int) ((fileDownloadProgresssModel.getTotalProgress() * 100) / fileDownloadProgresssModel.getFileLength());
        fileDownloadProgresssModel.setProgressPercentage(progress);
        p2PCallBacks.onProgressUpdate(fileDownloadProgresssModel);
        // Log.d("progressss..", "Progress :- " + progress + "%");
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("Receiver", "onPostExecute");
        try {
//      serverSocket.close();
            client.close();
            referenceCallback.call();
            p2PCallBacks.onFilesReceived(receivedFilesPathList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.d("Receiver", "Transfer Cancelled");
        try {
//      if (client.isConnected()) serverSocket.close();
            client.close();
            referenceCallback.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

