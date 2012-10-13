package com.abcrestaurant.ui;

interface TweenCallback {
    void onTweenValueChanged(float value, float oldValue);
    void onTweenStarted();
    void onTweenFinished();
}
