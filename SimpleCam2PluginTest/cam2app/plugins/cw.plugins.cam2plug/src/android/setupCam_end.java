/*

       public class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {
        private JSONObject error_codes;

         public MyCameraDeviceStateCallback() {
           super();
           notifyJs_String("errors","MyCameraDeviceStateCallback says: constructor called!");
           error_codes= new JSONObject();
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_DEVICE),      "ERROR_CAMERA_DEVICE");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_DISABLED),    "ERROR_CAMERA_DISABLED");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_IN_USE),      "ERROR_CAMERA_IN_USE");
           error_codes.put(JSONObject.numberToString(ERROR_CAMERA_SERVICE),     "ERROR_CAMERA_SERVICE");
           error_codes.put(JSONObject.numberToString(ERROR_MAX_CAMERAS_IN_USE), "ERROR_MAX_CAMERAS_IN_USE");
         }

         void onClosed (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onClosed called!");
         }

         void onOpen (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onOpen called!");
         }

         void onDisconnected (CameraDevice camera) {
           notifyJs_String("errors","MyCameraDeviceStateCallback says: onDisconnected called!");
         }

         void onError (CameraDevice camera, int error) {
           String txt_error = error_codes.get(JSONObject.numberToString(error));
           notifyJs_String("errors","MyCameraDeviceStateCallback says: "+txt_error);
         }

       }

       myCameraDeviceStateCallback = new MyCameraDeviceStateCallback();


       cameraManager.openCamera(cameraId,myCameraDeviceStateCallback,null);//Use current thread's looper for now


       public class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {
         public MyCameraCaptureSessionStateCallback() {
           super();
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: constructor called!");
         }

         void onActive (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onActive called!");
         }

         void onCaptureQueueEmpty (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onCaptureQueueEmpty called!");
         }

         void onClosed (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onClosed called!");
         }

         void onConfigureFailed (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onConfigureFailed called!");
         }

         void onConfigured (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onConfigured called!");
         }

         void onReady (CameraCaptureSession session) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onReady called!");
         }

         void onSurfacePrepared (CameraCaptureSession session, Surface surface) {
           notifyJs_String("errors","MyCameraCaptureSessionStateCallback says: onSurfacePrepared called!");
         }
       }
       myCameraCaptureSessionStateCallback = new MyCameraCaptureSessionStateCallback();




       public class MySurfaceTextureOnFrameAvailableListener implements SurfaceTexture.OnFrameAvailableListener {

         public MySurfaceTextureOnFrameAvailableListener() {
           notifyJs_String("errors","MySurfaceTextureOnFrameAvailableListener says: constructor called!");
         }

         public void onFrameAvailable(SurfaceTexture surfaceTexture) {
           notifyJs_String("errors","MySurfaceTextureOnFrameAvailableListener says: onFrameAvailable called!");
         }
       }
       mySurfaceTextureOnFrameAvailableListener = new MySurfaceTextureOnFrameAvailableListener();
*/


//      mySurfaceTexture = 
//      mySurface        = new Surface(mySurfaceTexture);

//      mySurfaceView   = SurfaceView(theActivity);
//      mySurfaceHolder = mySurfaceView.getHolder();
//      mySurface       = mySurfaceHolder.getSurface();

//createCaptureSession (List<Surface> outputs, myCameraCaptureSessionStateCallback, Handler handler)
