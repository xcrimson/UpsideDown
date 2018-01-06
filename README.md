## UpsideDown app

This app shows familiarity with basics of Android platform.

It allows user to input URL of a picture, downloads the picture, displays it upside down and stores it on a device.

## The WHYs

I used IntentService to download images because it is the easiest way to offload operations from the main thread using services.

I read data from clipboard in the onResume method because I think that most of the time picture URL will be copypasted from somewhere else and it would be nice to save user a couple of clicks this way.

I lock the download button until request completes to prevent accidential requests spam from user.

I fixed activity orientation because it is rare for modern application to do otherwise and it helped me to keep my solution simple.