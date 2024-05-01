package com.example.tallermovil_ii

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.tallermovil_ii.databinding.ActivityLoginBinding
import com.example.tallermovil_ii.databinding.ActivitySignUpBinding
import com.example.tallermovil_ii.model.DataBase
import com.example.tallermovil_ii.model.User
import com.example.tallermovil_ii.permission.Permission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {
    //VIEW
    lateinit var binding: ActivitySignUpBinding
    val TAG = "Signin";

    //PROFILE PHOTO
    var localPhoto: Uri? = null
    var url: String = ""
    private lateinit var photoUri: Uri

    //AUTH
    private lateinit var auth: FirebaseAuth

    //DATABASE
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference

    //MAPA
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var point: GeoPoint
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //INITIALIZE
        permisoUbicacion()
        myRef = database.getReference(DataBase.PATH_USER)
        auth = Firebase.auth
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = createLocationRequest()

        //SET LISTENNERS
        setListenners()
    }

    //LIFECYCLE
    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                Log.i("LOCATION", "onSuccess location")
                if (location != null) {
                    Log.i("LOCATION", "Longitud: " + location.longitude)
                    Log.i("LOCATION", "Latitud: " + location.latitude)
                    point = GeoPoint(location.latitude, location.longitude);
                } else {
                    Log.i("LOCATION", "FAIL location")
                }
            }
        }

    }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
    private fun stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }
    private fun createLocationRequest(): LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,10000).apply {
            setMinUpdateIntervalMillis(5000)
        }.build()
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }
    }

    //SET LISTENNERS
    private fun setListenners(){
        binding.btnSignUp.setOnClickListener {
            if(validate() && checkCredentials()){
                //guardar usuario en autenticacion
                saveuser()
                //guardar usuario en db
                //guardar imagen

            }else{
                Toast.makeText(this, "Invalid fields.", Toast.LENGTH_SHORT).show()
            }
        }

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                Log.i("LOCATION", "Location update in the callback: $location")
                if (location != null) {
                    point = GeoPoint(location.latitude, location.longitude)
                }
            }
        }

        binding.btnCamera.setOnClickListener { permissionCamera() }

        binding.btnGallery.setOnClickListener { permissionGallery() }
    }

    private fun saveuser(){
        auth.createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    val user = auth.currentUser
                    if (user != null) {
                        // Update user info
                        saveImage(user)
                        //updateUserinfo(user)
                    }
                } else {
                    Toast.makeText(this, "createUserWithEmail:Failure: " + task.exception.toString(),
                        Toast.LENGTH_SHORT).show()
                    task.exception?.message?.let { Log.e(TAG, it) }
                }
            }
    }

    private fun updateUserinfo(currentUser: FirebaseUser){
        val userInfo = User(binding.name.text.toString(),
            binding.lastName.text.toString(),
            binding.email.text.toString(),
            binding.password.text.toString(), url,
            binding.identification.text.toString(),
            point.latitude, point.longitude)


        myRef = database.getReference(DataBase.PATH_USER)
        myRef = database.getReference(DataBase.PATH_USER + "/" + currentUser.uid)
        myRef.setValue(userInfo)
    }

    private fun saveImage(currentUser: FirebaseUser){
        // Update user info
        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef = storageRef.child(DataBase.PATH_USER_IMAGES + "/" + currentUser.uid)

        // Crea una referencia única para la imagen (por ejemplo, usando el timestamp actual)
        val imageName = "image_${System.currentTimeMillis()}.jpg"
        val imageRef = imagesRef.child(imageName)

        // Sube la imagen al Firebase Storage
        imageRef.putFile(localPhoto!!).addOnSuccessListener(object :
            OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                // Get a URL to the uploaded content
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    url = uri.toString()
                    updateUserinfo(currentUser)
                    updateUI(currentUser)
                }.addOnFailureListener { exception ->
                    // Error al obtener la URL de descarga
                    Log.i("FBApp", "Failed saving url image:" )
                }
                Log.i("FBApp", "Successfully uploaded image")
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(exception: Exception) {
                // Handle unsuccessful uploads
                Log.i("FBApp", "Failed uploading image"+ exception.message)
            }
        })
    }
    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
            Toast.makeText(this, "ya inicio: " + currentUser.email, Toast.LENGTH_SHORT).show()
        } else {
            binding.email.setText("")
            binding.password.setText("")
            Toast.makeText(this, "NO EXISTE", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkCredentials(): Boolean{
        var valid = true

        if (!binding.email.text.toString().contains("@") ||
            !binding.email.text.toString().contains(".") ||
            binding.email.text.toString().length < 5){
            binding.email.error = "Invalid format."
            valid = false
        }else{
            binding.email.error = null
        }

        if (binding.password.text.toString().length < 8) {
            binding.password.error = "Insecure password."
            valid = false
        } else {
            binding.password.error = null
        }

        if (binding.identification.text.toString().toIntOrNull() == null) {
            binding.identification.error = "Identification must be a number."
            valid = false
        } else {
            binding.identification.error = null
        }

        return valid
    }
    private fun validate(): Boolean {
        var valid = true
        if (TextUtils.isEmpty(binding.name.text.toString())) {
            binding.name.error = "Required."
            valid = false
        } else {
            binding.name.error = null
        }

        if (TextUtils.isEmpty(binding.lastName.text.toString())) {
            binding.lastName.error = "Required."
            valid = false
        } else {
            binding.lastName.error = null
        }

        if (TextUtils.isEmpty(binding.email.text.toString())) {
            binding.email.error = "Required."
            valid = false
        } else {
            binding.email.error = null
        }

        if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = "Required."
            valid = false
        } else {
            binding.password.error = null
        }

        if (TextUtils.isEmpty(binding.identification.text.toString())) {
            binding.identification.error = "Required."
            valid = false
        } else {
            binding.identification.error = null
        }
        return valid
    }
    private fun permissionCamera(){
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePic()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "WE NEED PERMISSION TO ACCESS CAMERA", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Permission.MY_PERMISSION_REQUEST_CAMERA)
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Permission.MY_PERMISSION_REQUEST_CAMERA)
            }
        }
    }
    private fun takePic() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.tallerMovil-II.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePictureIntent, Permission.REQUEST_IMAGE_CAPTURE)
                }
            } else {
                Toast.makeText(this, "No hay una cámara disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No hay permiso de cámara", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), Permission.MY_PERMISSION_REQUEST_CAMERA)
        }
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private fun permissionGallery(){
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                selectPhoto()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Toast.makeText(this, "WE NEED PERMISSION TO ACCESS GALLERY", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Permission.MY_PERMISSION_REQUEST_GALLERY
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    Permission.MY_PERMISSION_REQUEST_GALLERY
                )
            }
        }
    }

    fun selectPhoto () {
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val pickImage = Intent(Intent.ACTION_PICK)
            pickImage.type = "image/*"
            startActivityForResult(pickImage, Permission.IMAGE_PICKER_REQUEST)
        } else {
            Toast.makeText(this, "NO PERMISSION TO ACCESS GALLERY", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Permission.MY_PERMISSION_REQUEST_GALLERY)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Permission.IMAGE_PICKER_REQUEST ->{
                if(resultCode == Activity.RESULT_OK){
                    try {
                        //Logica de seleccion de imagen
                        val selectedImageUri = data!!.data
                        if(data.data != null){
                            binding.photo.setImageURI(selectedImageUri)
                            localPhoto = selectedImageUri
                        }
                    } catch (e: FileNotFoundException){
                        e.printStackTrace()
                    }
                }
            }
            Permission.REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Load the full-quality image into ImageView
                    localPhoto = photoUri
                    binding.photo.setImageURI(photoUri)
                }
            }
        }
    }

    fun permisoUbicacion(){

        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "Requiere acceso a ubicación", Toast.LENGTH_SHORT).show()
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Permission.MY_PERMISSION_REQUEST_LOCATION)
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    Permission.MY_PERMISSION_REQUEST_LOCATION)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Permission.MY_PERMISSION_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    takePic()
                    Toast.makeText(this, "Permiso de camara concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de camara negado", Toast.LENGTH_SHORT).show()

                }
            }
            Permission.MY_PERMISSION_REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //selectPhoto()
                    Toast.makeText(this, "Permiso de galería concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
                }
            }
            Permission.MY_PERMISSION_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permiso de ubicación negado", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}