package com.example.cloud.fmoddemo.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.query.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by SX on 2017/8/1.
 */

public class DBManager {
    private String TAG = "DBManager";
    private final static String dbName = "meta_db";
    private static DBManager manager;
    private DaoMaster.DevOpenHelper openHelper;
    private Context context;

    private DBManager(Context context) {
        this.context = context;
    }

    public static DBManager getInstance(Context context) {
        if (manager == null) {
            synchronized (DBManager.class) {
                if (manager == null) {
                    manager = new DBManager(context);
                }
            }
        }
        return manager;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(context, dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    public <D> boolean insertBean(@NotNull D bean) {
        checkArgument(bean.getClass());
        AbstractDao dao = getBeanDao(bean.getClass());
        if (dao != null) {
            dao.insertOrReplace(bean);
            return true;
        } else {
            Log.e(TAG, "insertBean: 获取AbstractDao失败");
            return false;
        }
    }

    public <D> boolean insertBeanList(@NotNull List<D> list) {
        if (list == null || list.isEmpty()) {
            Log.e(TAG, "insertBeanList: list == null");
            return false;
        }
        checkArgument(list.get(0).getClass());
        AbstractDao beanDao = getBeanDao(list.get(0).getClass());
        if (beanDao == null) {
            beanDao.insertOrReplaceInTx(list);
            return true;
        } else {
            Log.e(TAG, "insertBeanList: dao ==null");
            return false;
        }
    }

    public <D> boolean deleteBeanByKey(Class<D> dClass, Object keyValue) {
        checkArgument(dClass);
        AbstractDao dao = getBeanDao(dClass);
        if (dao != null) {
            try {
//                Method getKey = dao.getClass().getDeclaredMethod("getKey");
//                getKey.setAccessible(true);
//                Object key = getKey.invoke(dao);
//                dao.deleteByKey(key);
                dao.deleteByKey(keyValue);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            Log.e(TAG, "insertBean: 获取AbstractDao失败");
            return false;
        }
        return true;
    }

    public <D> boolean deleteBeanAll(Class<D> dClass) {
        checkArgument(dClass);
        AbstractDao beanDao = getBeanDao(dClass);
        if (beanDao != null) {
            beanDao.deleteAll();
        } else {
            Log.e(TAG, "deleteBeanAll: dao==null");
            return false;
        }
        return true;
    }

    public <D> boolean updateBean(D bean) {
        checkArgument(bean.getClass());
        AbstractDao beanDao = getBeanDao(bean.getClass());
        if (beanDao != null) {
            beanDao.update(bean);
        } else {
            Log.e(TAG, "insertBean: 获取AbstractDao失败");
            return false;
        }
        return true;
    }
    public <D> D queryBeanByKey(Class<D> dClass, Object keyValue) {
        checkArgument(dClass);
        AbstractDao beanDao = getBeanDao(dClass);
        if (beanDao != null) {
            QueryBuilder queryBuilder = beanDao.queryBuilder();
            Property[] properties = beanDao.getProperties();
            Property key = null;
            for (Property property : properties) {
                if (property.primaryKey) {
                    key = property;
                    break;
                }
            }
            D  bean= (D) queryBuilder.where(key.eq(keyValue)).unique();
            Log.e(TAG, "queryBeanByKey: "+bean );
            return bean;
        }else{
            Log.e(TAG, "insertBean: 获取AbstractDao失败" );
        }
        return null;
    }
    public <D> List<D> queryBeanList(Class<D> dClass) {
        List<D> list = null;
        checkArgument(dClass);
        AbstractDao beanDao = getBeanDao(dClass);
        if(beanDao!=null) {
            QueryBuilder queryBuilder = beanDao.queryBuilder();
            Property[] properties = beanDao.getProperties();
            Property key = null;
            for (Property property : properties) {
                if (property.primaryKey) {
                    key = property;
                    break;
                }
            }
            list = queryBuilder.list();

            if (key != null) {
                list = queryBuilder.orderDesc(key).list();
            } else {
                Log.e(TAG, "queryBeanList: 获取KEY失败");
            }
        }else{
            Log.e(TAG, "insertBean: 获取AbstractDao失败" );
        }
        return list;
    }

    private <D> void checkArgument(Class<D> dClass) {
        Entity annotation = dClass.getAnnotation(Entity.class);
        if (annotation == null) {
            new IllegalArgumentException("参数不是GreenDao表的实体（未注解Entity）");
        }
    }

    private <D> AbstractDao getBeanDao(Class<D> dClass) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        AbstractDao dao = null;
        try {
            Class<? extends DaoSession> aClass = daoSession.getClass();
            Method declaredMethod = aClass.getDeclaredMethod("get" + dClass.getSimpleName() + "Dao");
            dao = (AbstractDao) declaredMethod.invoke(daoSession);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dao;
    }

//    public void insertFamilyItemBean(MusicBean familyItemBean) {
//        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
//        familyItemBeanDao.insertOrReplace(familyItemBean);
//    }
//
//    public void insertFamilyItemBeanList(List<FamilyItemBean> familyItemBeanList) {
//        if (familyItemBeanList == null || familyItemBeanList.isEmpty()) {
//            return;
//        }
//        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
//        familyItemBeanDao.insertOrReplaceInTx(familyItemBeanList);
//    }
//
//    public void deleteFamilyItemBean(FamilyItemBean familyItemBean) {
//        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
////        familyItemBeanDao.delete(familyItemBean);
//        familyItemBeanDao.deleteByKey(familyItemBean.getFace_id());
//    }
//
//    public void deleteFamilyItemBeanAll() {
//        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
//        familyItemBeanDao.deleteAll();
//    }
//
//    public void updateFamilyItemBean(FamilyItemBean familyItemBean) {
//        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
//        familyItemBeanDao.update(familyItemBean);
//    }
//
//    public List<FamilyItemBean> queryFamilyItemBeanList() {
//        DaoMaster daoMaster = new DaoMaster(getReadableDatabase());
//        DaoSession daoSession = daoMaster.newSession();
//        FamilyItemBeanDao familyItemBeanDao = daoSession.getFamilyItemBeanDao();
//        QueryBuilder<FamilyItemBean> qb = familyItemBeanDao.queryBuilder();
//        List<FamilyItemBean> list = qb.orderDesc(FamilyItemBeanDao.Properties.Face_id).list();
//        return list;
//    }
}
