package com.example.travelappbe.service;

import org.springframework.stereotype.Service;

@Service
public class GeoHashService {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int PRECISION = 8;

    public String encode(double latitude, double longitude) {
        boolean isEven = true;
        int bit = 0;
        int ch = 0;
        StringBuilder geohash = new StringBuilder();

        double[] latRange = { -90.0, 90.0 };
        double[] lonRange = { -180.0, 180.0 };

        while (geohash.length() < PRECISION) {
            double mid;
            if (isEven) {
                mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude >= mid) {
                    ch |= 1 << (4 - bit);
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2;
                if (latitude >= mid) {
                    ch |= 1 << (4 - bit);
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }

        return geohash.toString();
    }
}
