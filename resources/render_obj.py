import bpy, math, sys

# Get .obj file path
argv = sys.argv
objPath = argv[argv.index("--") + 1]  # get all args after "--"
outputPath = argv[argv.index("--") + 2] 

# Create new scene
bpy.ops.scene.new(type="NEW")

# Import obj file
bpy.ops.import_scene.obj(filepath=objPath)

# Add new camera
bpy.ops.object.camera_add()

# Set the camera as the scene camera
camera = bpy.data.objects["Camera.001"]
bpy.context.scene.camera = camera

# Select the plot object
plot = bpy.data.objects[0]
plot.select_set(True)

# Move camera to have a good view
camera.rotation_euler = [math.radians(54.7), math.radians(0), math.radians(45)]
bpy.ops.view3d.camera_to_view_selected()

# Add a light at the same location as the camera
bpy.ops.object.light_add(type="SUN", location=camera.location, rotation=camera.rotation_euler)

# Render to PNG
bpy.context.scene.render.filepath = outputPath
bpy.context.scene.render.film_transparent = True
bpy.ops.render.render(write_still=True)
bpy.ops.wm.quit_blender()
