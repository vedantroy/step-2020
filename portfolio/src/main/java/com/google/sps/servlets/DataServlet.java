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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.files.FileServicePb.FileContentType.ContentType;
import com.google.gson.Gson;

/** Servlet that returns some example content.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String CONTENT_TYPE_JSON = "application/json;";
  private static final String PATH_HOME_PAGE = "/index.html";
  private static final int DEFAULT_MAX_COMMENTS = 5;
  private static final String PARAM_MAX_COMMENTS = "max_comments";
  private static final String KEY_COMMENT_VALUE = "value";
  private static final String KIND_COMMENT = "comment";
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public DataServlet() {
    super();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String comment = req.getParameter(KIND_COMMENT);
    if (comment != null && !comment.equals("")) {
      // do nothing if the comment field is empty
      Entity commentEntity = new Entity(KIND_COMMENT);
      commentEntity.setProperty(KEY_COMMENT_VALUE, comment);
      datastore.put(commentEntity);
    }
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
    Query query = new Query(KIND_COMMENT);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));
    String[] comments = results
                           .stream()
                           .map(e -> e.getProperty(KEY_COMMENT_VALUE))
                           .toArray(String[]::new);
    response.setContentType(CONTENT_TYPE_JSON);
    response.getWriter().println(new Gson().toJson(comments));
    response.setStatus(200);
  }
}
