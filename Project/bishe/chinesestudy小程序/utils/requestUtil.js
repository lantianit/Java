// 定义公共的url
const baseUrl="http://127.0.0.1:8080";
/**
 * 返回baseUrl
 */
export const getBaseUrl=()=>{
  return baseUrl;
}

/**
 * 后端请求工具类
 * @param {*} params 请求参数
 */
export const requestUtil=(params)=>{ 
  return new Promise((resolve,reject)=>{
    wx.request({
     ...params,
     url:baseUrl+params.url,
     success:(result)=>{
       resolve(result.data);
     },
     fail:(err)=>{
       reject(err);
     }
    })
  });
}