package com.mky1428.tp12kakaoplacesearchapp;

import java.util.List;

public class SearchLocalApiResponse {
    PlaceMeta meta;
    List<Place> documents;
}

class PlaceMeta{
    int total_count;
    int pageable_count;
    boolean is_end;
}

class Place{
    String place_name;
    String address_name;
    String road_address_name;
    String place_url;
    String x;
    String y;
    String distance;
}


