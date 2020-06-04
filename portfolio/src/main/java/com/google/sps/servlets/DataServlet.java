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
import com.google.gson.Gson;

/** Servlet that returns some example content.*/
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final String COMMENT_KIND = "comment";

  public DataServlet() {
    super();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String comment = req.getParameter(COMMENT_KIND);
    if (comment != null && !comment.equals("")) {
      // do nothing if the comment field is empty
      Entity commentEntity = new Entity(COMMENT_KIND);
      commentEntity.setProperty("value", comment);
      datastore.put(commentEntity);
    }
    resp.sendRedirect("/index.html");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String maxCommentsString = request.getParameter("max_comments");
    if (maxCommentsString != null) {
      Integer maxComments = Integer.parseInt(maxCommentsString);
      Query query = new Query(COMMENT_KIND);
      List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(maxComments));
      String[] comments = results
                             .stream()
                             .map(e -> e.getProperty("value"))
                             .toArray(String[]::new);
      response.setContentType("application/json;");
      response.getWriter().println(new Gson().toJson(comments));
      response.setStatus(200);
    } else {
      response.sendError(400);
    }
  }
}
