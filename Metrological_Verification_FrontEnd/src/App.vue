<template>
  <div id="app">
    <el-container>
      <el-header
      background-color="red !important"
      color="red">
        <div style="background-color:blue !important">
          <div class="logo"></div>
          <h1 class="nav">检测云</h1>
          <!-- <el-menu  class="nav el-menu-header" mode="horizontal"
        background-color="#545c64" router
        text-color="#fff">
        <el-menu-item index="/"> Home</el-menu-item>
        <el-menu-item index="/task/list">任务列表</el-menu-item>
        <el-menu-item index="/template/list">模版列表</el-menu-item> -->
          <!-- </el-menu> -->
        </div>
      </el-header>
      <el-container>
        <el-aside :width="isCollapse?'65px':'186px'" style="transition:width 0.5s">
        <!-- <el-aside width="unset" -->
        <!-- bac> -->
          <el-menu
            default-active="1-4-1"
            class="el-menu-vertical-demo"
            @open="handleOpen"
            @close="handleClose"
            @select="handleSelect"
            :collapse="isCollapse"
            router="true"
            background-color="rgb(0,21,41)"
            text-color="rgb(255,255,255)"
          >
            <!-- <el-menu-item index="0">
          <i class="el-icon-menu"></i>
        </el-menu-item>
        <el-submenu index="1">
          <template slot="title">
            <i class="el-icon-location"></i>
            <span slot="title">任务列表</span>
          </template>
          <el-menu-item-group>
            <span slot="title">分组一</span>
            <el-menu-item index="1-1">选项1</el-menu-item>
            <el-menu-item index="1-2">选项2</el-menu-item>
          </el-menu-item-group>
          <el-menu-item-group title="分组2">
            <el-menu-item index="1-3">选项3</el-menu-item>
          </el-menu-item-group>
          <el-submenu index="1-4">
            <span slot="title">选项4</span>
            <el-menu-item index="1-4-1">选项1</el-menu-item>
          </el-submenu>
        </el-submenu> -->
            <el-menu-item index="/task/list">
              <i class="el-icon-document"></i>
              <span slot="title">任务列表</span>
            </el-menu-item>
            <el-menu-item index="/template/list">
              <i class="el-icon-setting"></i>
              <span slot="title">模板列表</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "App",
  components: {},
  data: function () {
    return {
      isCollapse: false,
    };
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath);
    },
    handleSelect(key) {
      if (key === "0") {
        this.isCollapse = !this.isCollapse;
        setTimeout(function () {
          window.dispatchEvent(new Event("resize"));
        }, 700);
      }
    },
  },
};
</script>


<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  color: #2c3e50;
}

/* #nav {
  padding: 30px;
} */

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}

html,
body,
#app,
.el-container {
  height: 100%;
  margin: 0;
}

.el-header,
.el-footer {
  background-color: #545c64;
  color: #333;
  text-align: center;
  line-height: 60px;
  padding: 0;
}

.el-aside {
  background-color: #d3dce6;
  color: #333;
  /* text-align: center; */
  /* line-height: 200px; */
}

.el-main {
  background-color: #e9eef3;
  color: #333;
  /* text-align: center; */
  /* line-height: 160px; */
}

body > .el-container {
  margin-bottom: 40px;
}

.nav {
  text-align: left;
  vertical-align: middle;
  /* // font-size: 30px; */
  font-weight: bold;
  /* background-color: #191919; */
  font-family: PingFangSC-Regular;
  font-size: 25px;
  line-height: 60px;
  color: #ffffff;
  cursor: pointer;
  font-weight: bold;
  padding: 0 20px 0 16px;
  margin-left: -10px;
  float: left;
}
.el-header h1 {
  min-width: 200px;
  color: white;
  margin: 0px;
}
.el-menu{
  border-right:0;
}
.el-menu-vertical-demo:not(.el-menu--collapse) {
  width: 185px;
  border-right:0;
}

.goback {
  cursor: pointer;
}
.el-button-group {
  margin-right: 10px;
}
.toolbar {
  margin: 10px 0;
}
.logo {
  margin-top: 5px;
  width: 160px;
  height: 60px;
  float: left;
  border: 0 1 0 0;
  border-right: #ffffff;

  background: url("./assets/logo.svg") no-repeat;
}
img {
  border: 0;
  width: 100%;
  height: 100%;
  margin: -1px;
  border-style: none;
}
.el-header {
    background-color: rgb(0,21,41);
    color: #333;
    line-height: 60px;
    padding: 0px,0px !important;
    border-right: 0;
  }
.el-aside{
  background-color: rgb(0,21,41);
    color: #333;
    line-height: 60px;
}
</style>
