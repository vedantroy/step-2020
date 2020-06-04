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

  private static final String JSON_CONTENT_TYPE = "application/json;";
  private static final String HOME_PAGE_PATH = "/index.html";
  private static final int DEFAULT_MAX_COMMENTS = 5;
  private static final String MAX_COMMENTS_PARAM = "max_comments";
  private static final String COMMENT_VALUE_KEY = "value";
  private static final String COMMENT_KIND = "comment";
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  public DataServlet() {
    super();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String comment = req.getParameter(COMMENT_KIND);
    if (comment != null && !comment.equals("")) {
      // do nothing if the comment field is empty
      Entity commentEntity = new Entity(COMMENT_KIND);
      commentEntity.setProperty(COMMENT_VALUE_KEY, comment);
      datastore.put(commentEntity);
    }
    resp.sendRedirect(HOME_PAGE_PATH);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String maxCommentsString = request.getParameter(MAX_COMMENTS_PARAM);
    Integer maxComments = null;
    try {
      maxComments = Integer.parseInt(maxCommentsString);
    } catch (NumberFormatException e) {
      maxComments = DEFAULT_MAX_COMMENTS;
    }
    Query query = new Query(COMMENT_KIND);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));
    String[] comments = results
                           .stream()
                           .map(e -> e.getProperty(COMMENT_VALUE_KEY))
                           .toArray(String[]::new);
    response.setContentType(JSON_CONTENT_TYPE);
    response.getWriter().println(new Gson().toJson(comments));
    response.setStatus(200);
  }
}
