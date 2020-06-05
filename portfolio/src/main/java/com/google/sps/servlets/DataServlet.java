// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.gson.Gson;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String CONTENT_TYPE_JSON = "application/json;";
  private static final String PATH_HOME_PAGE = "/index.html";
  private static final int DEFAULT_MAX_COMMENTS = 5;
  private static final String PARAM_MAX_COMMENTS = "max_comments";
  private static final String KEY_COMMENT_BLOB_KEY = "blobKey";
  private static final String KEY_COMMENT_IMAGE_URL = "url";
  private static final String KEY_COMMENT_TEXT = "text";
  private static final String KIND_FILE_COMMENT = "comment";

  private static final String FORM_FIELD_COMMENT_IMAGE = "image";
  private static final String FORM_FIELD_COMMENT_TEXT = "text";

  private final BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public DataServlet() {
    super();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Map<String, List<BlobKey>> blobs = blobService.getUploads(req);
    List<BlobKey> blobKeys = blobs.get(FORM_FIELD_COMMENT_IMAGE);

    String comment = req.getParameter(FORM_FIELD_COMMENT_TEXT);
    if (comment == null || comment.equals("")) {
      // Don't do anything (except cleanup) if the comment is null/empty
      // it seems reasonable to force the user to provide a comment
      // for their image.
      if (blobKeys != null && !blobKeys.isEmpty()) {
        for (BlobKey k : blobKeys) {
          blobService.delete(k);
        }
      }
      resp.setStatus(400);
      return;
    }

    Entity commentEntity = new Entity(KIND_FILE_COMMENT);
    commentEntity.setProperty(KEY_COMMENT_TEXT, comment);

    if (blobKeys != null && !blobKeys.isEmpty()) {
      // The form only contains a single file input
      BlobKey blobKey = blobKeys.get(0);

      for (int i = 1; i < blobKeys.size(); ++i) {
        BlobKey k = blobKeys.get(i);
        blobService.delete(k);
      }

      BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
      if (blobInfo.getSize() != 0) {
        ImagesService imagesService = ImagesServiceFactory.getImagesService();
        ServingUrlOptions opts = ServingUrlOptions.Builder.withBlobKey(blobKey);

        String imageUrl;
        try {
          URL url = new URL(imagesService.getServingUrl(opts));
          imageUrl = url.getPath();
        } catch (MalformedURLException e) {
          imageUrl = imagesService.getServingUrl(opts);
        }
        commentEntity.setProperty(KEY_COMMENT_IMAGE_URL, imageUrl);
        commentEntity.setProperty(KEY_COMMENT_BLOB_KEY, blobKey);
      } else {
        // the user didn't upload a file, cleanup
        blobService.delete(blobKey);
      }
    }
    datastore.put(commentEntity);
    resp.sendRedirect(PATH_HOME_PAGE);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String maxCommentsString = request.getParameter(PARAM_MAX_COMMENTS);
    int maxComments = DEFAULT_MAX_COMMENTS;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      System.err.print(e);
    }
    Query query = new Query(KIND_FILE_COMMENT);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));
    Map<String, String>[] comments = results.stream().map(e -> {
      Map<String, String> m = new HashMap<>();
      String text = (String) e.getProperty(KEY_COMMENT_TEXT);
      m.put(KEY_COMMENT_TEXT, text);
      String url = (String) e.getProperty(KEY_COMMENT_IMAGE_URL);
      m.put(KEY_COMMENT_IMAGE_URL, url);
      return m;
    }).toArray(HashMap[]::new);
    response.setContentType(CONTENT_TYPE_JSON);
    response.getWriter().println(new Gson().toJson(comments));
    response.setStatus(200);
  }
}
