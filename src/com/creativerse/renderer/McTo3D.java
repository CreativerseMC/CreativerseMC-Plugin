package com.creativerse.renderer;

import com.creativerse.commands.Save;

import com.sk89q.worldedit.regions.CuboidRegion;
import io.github.oguzhancevik.obj2gltf.ConvertObjToGltf;
import io.github.oguzhancevik.obj2gltf.obj.BufferStrategy;
import io.github.oguzhancevik.obj2gltf.obj.GltfWriteType;
import io.github.oguzhancevik.obj2gltf.obj.IndicesComponentType;
import org.bukkit.Bukkit;
import org.jmc.ObjExporter;
import org.jmc.Options;

import java.io.File;

public class McTo3D {

    public static File create3DModel(CuboidRegion region, String name, String worldName) {
        // Creates 3D model of plot for NFT thumbnail
        Options.worldDir = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/" + worldName);
        Options.outputDir = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache");
        Options.minX = region.getPos1().getX(); Options.minY = region.getMinimumY(); Options.minZ = region.getPos1().getZ();
        Options.maxX = region.getPos2().getX() + 1; Options.maxY = region.getMaximumY(); Options.maxZ = region.getPos2().getZ() + 1;
        Options.renderUnknown = true;
        Options.optimiseGeometry = true;
        Options.renderSides = true;
        Options.renderBiomes = true;
        Options.objFileName = name + ".obj";
        Options.mtlFileName = name + ".mtl";
        Options.resourcePacks.add(new File(Save.class.getProtectionDomain().getCodeSource().getLocation().getPath()));

        ObjExporter.export(null, null, true, true, true);
        File objFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/" + name + ".obj");
        File mtlFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/" + name + ".mtl");

        objFile.deleteOnExit();
        mtlFile.deleteOnExit();

        return objFile;
    }

    public static File convertObjToGltf(String name) {
        String path = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/").getAbsolutePath();
        String inputPath = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/" + name + ".obj").getAbsolutePath();

        ConvertObjToGltf convertObjToGltf = new ConvertObjToGltf.Builder().inputObjFilePath(inputPath)
                .inputMtlFileName(name).outputFilePath(path).outputFileName(name)
                .bufferStrategy(BufferStrategy.BUFFER_PER_FILE).indicesComponentType(IndicesComponentType.GL_UNSIGNED_SHORT).gltfWriteType(GltfWriteType.EMBEDDED).build();

        convertObjToGltf.convert();

        File gltfFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/" + name + ".gltf");
        gltfFile.deleteOnExit();

        return gltfFile;
    }

}
