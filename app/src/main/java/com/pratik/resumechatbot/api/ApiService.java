package com.pratik.resumechatbot.api;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import com.pratik.resumechatbot.model.QueryRequest;
import com.pratik.resumechatbot.model.QueryResponse;
import com.pratik.resumechatbot.model.ResumeListResponse;

public interface ApiService {
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadResume(@Part MultipartBody.Part file);

    @GET("resumes")
    Call<ResumeListResponse> getResumes();

    @DELETE("resumes/{filename}")
    Call<ResponseBody> deleteResume(@Path("filename") String filename);

    @POST("query")
    Call<QueryResponse> queryResume(@Body QueryRequest request);

    @POST("rebuild")
    Call<ResponseBody> rebuildIndex();
}
