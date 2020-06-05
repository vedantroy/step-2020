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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import com.google.sps.Event;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Comparator<Event> comparator = new Comparator<Event>() {
      @Override
      public int compare(final Event e1, final Event e2) {
        return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
      }
    };
    List<Event> l = new ArrayList(events);
    Collections.sort(l, comparator);
    Collection<TimeRange> ranges = new ArrayList<TimeRange>();
    int start = TimeRange.START_OF_DAY;
    for (Event e : l) {
      Set<String> attendees = e.getAttendees();
      Set<String> copy = new HashSet<>(attendees);
      copy.retainAll(request.getAttendees());
      if (copy.size() > 0) {
        int end = e.getWhen().start();
        if (end - start >= request.getDuration()) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        }
        int potentialNewStart = e.getWhen().end();
        if (potentialNewStart > start) {
          start = potentialNewStart;
        }
      }     
    }
    if (TimeRange.END_OF_DAY - start >= request.getDuration()) {
      ranges.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return ranges;
  }
}
