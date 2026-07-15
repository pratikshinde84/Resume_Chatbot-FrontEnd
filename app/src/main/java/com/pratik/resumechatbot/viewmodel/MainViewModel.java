package com.pratik.resumechatbot.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pratik.resumechatbot.api.RetrofitClient;
import com.pratik.resumechatbot.model.Message;
import com.pratik.resumechatbot.model.QueryRequest;
import com.pratik.resumechatbot.model.QueryResponse;
import com.pratik.resumechatbot.model.ResumeListResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {
    private static final String TAG = "MainViewModel";
    private final MutableLiveData<List<String>> resumes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    public LiveData<List<String>> getResumes() { return resumes; }
    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getSuccessMessage() { return successMessage; }

    public void fetchResumes() {
        isLoading.setValue(true);
        Log.d(TAG, "Fetching resumes...");
        RetrofitClient.getApiService().getResumes().enqueue(new Callback<ResumeListResponse>() {
            @Override
            public void onResponse(Call<ResumeListResponse> call, Response<ResumeListResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<String> list = response.body().getResumes();
                    Log.d(TAG, "Resumes fetched: " + list);
                    resumes.setValue(list);
                    if (list == null || list.isEmpty()) {
                        error.setValue("No resumes found in the folder");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch resumes: " + response.code());
                    error.setValue("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResumeListResponse> call, Throwable t) {
                isLoading.setValue(false);
                Log.e(TAG, "Error fetching resumes", t);
                error.setValue("Connection failed: " + t.getMessage());
            }
        });
    }

    public void uploadResume(File file) {
        isLoading.setValue(true);
        Log.d(TAG, "Uploading file: " + file.getName());
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        RetrofitClient.getApiService().uploadResume(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "Upload successful");
                    successMessage.setValue("File uploaded successfully!");
                    fetchResumes();
                } else {
                    Log.e(TAG, "Upload failed: " + response.code());
                    error.setValue("Upload failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                Log.e(TAG, "Error uploading file", t);
                error.setValue(t.getMessage());
            }
        });
    }

    public void deleteResume(String filename) {
        isLoading.setValue(true);
        RetrofitClient.getApiService().deleteResume(filename).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    fetchResumes();
                } else {
                    error.setValue("Delete failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }

    public void rebuildIndex() {
        isLoading.setValue(true);
        Log.d(TAG, "Rebuilding index...");
        RetrofitClient.getApiService().rebuildIndex().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "Index rebuild successful");
                } else {
                    Log.e(TAG, "Rebuild failed: " + response.code());
                    error.setValue("Rebuild failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoading.setValue(false);
                Log.e(TAG, "Error rebuilding index", t);
                error.setValue(t.getMessage());
            }
        });
    }

    public void sendQuery(String text) {
        List<Message> current = messages.getValue();
        current.add(new Message(text, true));
        messages.setValue(current);

        isLoading.setValue(true);
        RetrofitClient.getApiService().queryResume(new QueryRequest(text)).enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> updated = messages.getValue();
                    updated.add(new Message(response.body().getAnswer(), false));
                    messages.setValue(updated);
                } else {
                    error.setValue("Query failed");
                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(t.getMessage());
            }
        });
    }
}
