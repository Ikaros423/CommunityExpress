import { createApp } from 'vue';
import { createPinia } from 'pinia';
import { createRouter } from './router';
import App from './App.vue';
import './styles.css';
import {
  create,
  NButton,
  NConfigProvider,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NMessageProvider,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NText
} from 'naive-ui';

const app = createApp(App);
app.use(createPinia());
app.use(createRouter());

const naive = create({
  components: [
    NButton,
    NConfigProvider,
    NDataTable,
    NForm,
    NFormItem,
    NInput,
    NInputNumber,
    NLayout,
    NLayoutContent,
    NLayoutHeader,
    NLayoutSider,
    NMenu,
    NMessageProvider,
    NModal,
    NSelect,
    NSpace,
    NSwitch,
    NText
  ]
});

app.use(naive);
app.mount('#app');
