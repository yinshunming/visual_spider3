import { createRouter, createWebHistory } from 'vue-router'
import TaskList from '../views/TaskList.vue'
import TaskConfig from '../views/TaskConfig.vue'

const routes = [
  {
    path: '/',
    redirect: '/tasks'
  },
  {
    path: '/tasks',
    name: 'TaskList',
    component: TaskList
  },
  {
    path: '/tasks/new',
    name: 'TaskNew',
    component: TaskConfig
  },
  {
    path: '/tasks/:id',
    name: 'TaskEdit',
    component: TaskConfig,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
