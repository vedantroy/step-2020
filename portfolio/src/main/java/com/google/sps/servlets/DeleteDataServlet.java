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
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  private static final String KEY_COMMENT_BLOB_KEY = "blobKey";
  private static final String KIND_FILE_COMMENT = "comment";
  private final BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public DeleteDataServlet() {
    super();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      Query query = new Query(KIND_FILE_COMMENT);
      PreparedQuery results = datastore.prepare(query);
      for (Entity entity : results.asIterable()) {
        Key entityKey = entity.getKey();
        BlobKey blobKey = (BlobKey) entity.getProperty(KEY_COMMENT_BLOB_KEY);
        if (blobKey != null) {
          blobService.delete(blobKey);
        }
        datastore.delete(entityKey);
      }
  }
}
