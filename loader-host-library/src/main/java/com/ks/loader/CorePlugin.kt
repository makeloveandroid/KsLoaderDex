package com.ks.loader

object CorePlugin {
  @JvmStatic
  fun createClassLoader(oldClassLoader: ClassLoader): ClassLoader {
    return  RePluginClassLoader(oldClassLoader.parent, oldClassLoader)
  }
}