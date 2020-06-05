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
import java.util.Arrays;

import com.google.sps.Event;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> mandatoryAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    Set<String> allAttendees = new HashSet<>(mandatoryAttendees);
    allAttendees.addAll(optionalAttendees);
    
    Collection<TimeRange> ranges = getTimeRanges(events, allAttendees, request.getDuration());
    if (ranges.size() > 0) {
      return ranges;
    }
    if (mandatoryAttendees.size() > 0) {
      return getTimeRanges(events, mandatoryAttendees, request.getDuration());
    }
    return Arrays.asList();
  }

  public Collection<TimeRange> getTimeRanges(Collection<Event> events, Collection<String> attendees, long duration) {
    Comparator<Event> comparator = new Comparator<Event>() {
      @Override
      public int compare(final Event e1, final Event e2) {
        return TimeRange.ORDER_BY_START.compare(e1.getWhen(), e2.getWhen());
      }
    };
    boolean rejectedEvent = false;
    List<Event> l = new ArrayList(events);
    Collections.sort(l, comparator);
    Collection<TimeRange> ranges = new ArrayList<TimeRange>();
    int start = TimeRange.START_OF_DAY;
    for (Event e : l) {
      Set<String> eventAttendees = e.getAttendees();
      Set<String> copy = new HashSet<>(eventAttendees);
      copy.retainAll(attendees);
      if (copy.size() > 0) {
        int end = e.getWhen().start();
        if (end - start >= duration) {
          ranges.add(TimeRange.fromStartEnd(start, end, false));
        } else {
          rejectedEvent = true;
        }
        int potentialNewStart = e.getWhen().end();
        if (potentialNewStart > start) {
          start = potentialNewStart;
        }
      }     
    }
    if (TimeRange.END_OF_DAY - start >= duration && (start != TimeRange.START_OF_DAY || !rejectedEvent)) {
      ranges.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }
    return ranges;
  }
}
