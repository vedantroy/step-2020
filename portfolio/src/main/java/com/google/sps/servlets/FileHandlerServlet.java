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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/files")
public class FileHandlerServlet extends HttpServlet {

  private static final String CONTENT_TYPE_JSON = "application/json;";
  private static final String KEY_IMAGE_BLOBKEY = "blobKey";
  private static final String KEY_IMAGE_URL = "url";
  private static final String KIND_IMAGE = "image";
  private static final String PATH_HOME_PAGE = "/index.html";
  private final BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    List<Entity> files = datastore.prepare(new Query(KIND_IMAGE)).asList(FetchOptions.Builder.withDefaults());
    String[] urls = files.stream().map(e -> e.getProperty(KEY_IMAGE_URL)).toArray(String[]::new);
    resp.setContentType(CONTENT_TYPE_JSON);
    resp.getWriter().println(new Gson().toJson(urls));
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Map<String, List<BlobKey>> blobs = blobService.getUploads(req);
    List<BlobKey> blobKeys = blobs.get(KIND_IMAGE);

    if (blobKeys == null || blobKeys.isEmpty()) {
      // the user didn't upload a file, do nothing
      resp.sendRedirect(PATH_HOME_PAGE);
      return;
    }

    // The form only contains a single file input
    BlobKey blobKey = blobKeys.get(0);
    for (int i = 1; i < blobKeys.size(); ++i) {
      BlobKey k = blobKeys.get(i);
      blobService.delete(k);
    }

    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      // the user didn't upload a file, cleanup
      blobService.delete(blobKey);
      resp.sendRedirect(PATH_HOME_PAGE);
      return;
    }

    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions opts = ServingUrlOptions.Builder.withBlobKey(blobKey);

    String imageUrl;
    try {
      URL url = new URL(imagesService.getServingUrl(opts));
      imageUrl = url.getPath();
    } catch (MalformedURLException e) {
      imageUrl = imagesService.getServingUrl(opts);
    }

    Entity imageUploadEntity = new Entity(KIND_IMAGE);
    imageUploadEntity.setProperty(KEY_IMAGE_URL, imageUrl);
    imageUploadEntity.setProperty(KEY_IMAGE_BLOBKEY, blobKey);
    datastore.put(imageUploadEntity);
    resp.sendRedirect(PATH_HOME_PAGE);
  }
}