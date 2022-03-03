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
lightrot = camera.rotation_euler.copy()
lightrot.x = math.radians(60)
lightrot.z = math.radians(60)
bpy.ops.object.light_add(type="SUN", location=camera.location, rotation=lightrot)

# Render to PNG
bpy.context.scene.render.filepath = outputPath
bpy.context.scene.render.film_transparent = True
bpy.context.scene.render.resolution_percentage = 400
bpy.context.scene.render.use_border = True
bpy.context.scene.render.use_crop_to_border = True
bpy.context.scene.render.border_max_x = 0.75
bpy.context.scene.render.border_min_x = 0.25
bpy.ops.render.render(write_still=True)
bpy.ops.wm.quit_blender()