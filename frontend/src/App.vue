<template>
  <div id="app">
    <el-container>
      <el-header style="border-bottom: 1px solid #e6e6e6;">
        <h2 style="margin: 0; line-height: 60px;">Visual Spider - 可视化爬虫配置</h2>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
body {
  margin: 0;
  padding: 0;
}
</style>
