import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    userInfo: null
  },
  mutations: {
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    },
    CLEAR_USER_INFO(state) {
      state.userInfo = null
    }
  },
  actions: {
    setUserInfo({ commit }, userInfo) {
      commit('SET_USER_INFO', userInfo)
    },
    clearUserInfo({ commit }) {
      commit('CLEAR_USER_INFO')
    }
  },
  getters: {
    getUserInfo: state => state.userInfo,
    isAuthenticated: state => !!state.userInfo
  }
})