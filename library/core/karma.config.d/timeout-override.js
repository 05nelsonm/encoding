// Override test timeout. Needed for stress tests
config.set({
  "client": {
    "mocha": {
      "timeout": 30000
    },
  },
  "browserDisconnectTimeout": 30000
});
