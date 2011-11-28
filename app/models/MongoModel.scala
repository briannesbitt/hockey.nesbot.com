package models

import com.mongodb.Mongo
import org.bson.types.ObjectId
import com.google.code.morphia.{Datastore, Morphia}
import scalaj.collection.Imports._
import com.google.code.morphia.dao.BasicDAO
import com.google.code.morphia.annotations.{Transient, Id}
import com.google.code.morphia.mapping.Mapper
import com.google.code.morphia.query.{QueryResults, UpdateOperations, Query}
import com.google.code.morphia.logging.MorphiaLoggerFactory
import scala.Predef._
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory

trait MorphiaMongo {
  val mongo : Mongo = new Mongo
  val morphia : Morphia = new Morphia()
  protected var _ds : Datastore = null

  def mapPackage(packageName:String) : MorphiaMongo = {morphia.mapPackage(packageName); this}
  def map[T](clazz: Class[T]) : MorphiaMongo = {morphia.map(clazz); this}
  def indexes = ds.ensureIndexes()
  def ds = {
    if (_ds == null) throw new NullPointerException("You have to initialize the DB first!")
    _ds
  }
}

object MongoDB extends MorphiaMongo {
  def init(database: String): MorphiaMongo = {
    MorphiaLoggerFactory.reset()
    MorphiaLoggerFactory.registerLogger(classOf[SLF4JLogrImplFactory]);
    _ds = morphia.createDatastore(mongo, database);
    this
  }
}

class MyDAO[T,K](val c: Class[T], val datastore: Datastore) extends BasicDAO[T,K](c, datastore)

abstract class MongoModel[T](implicit m: Manifest[T]) {
  @Transient private val _clz: Class[T] = m.erasure.asInstanceOf[Class[T]]
  @Id var id : ObjectId = _

  @Transient protected val _dao: MyDAO[T, ObjectId] = new MyDAO[T, ObjectId](_clz, MongoDB.ds)

  private def cast : T = this.asInstanceOf[T]

  def isPersistent = id != null
  def save : T = {_dao.save(cast); cast}

  protected def createQueryToFindMe : Query[T] = {
    if (!isPersistent) throw new IllegalStateException("Can't perform query on myself until I have been saved!")
    _dao.createQuery.field(Mapper.ID_KEY).equal(id)
  }
  protected def update(ops: UpdateOperations[T]) { _dao.updateFirst(createQueryToFindMe, ops) }
  protected def update(query: Query[T], ops: UpdateOperations[T]) { _dao.update(query, ops) }

  def delete = if (isPersistent) _dao.delete(cast)
}

abstract class MongoObject[T](implicit m: Manifest[T]) {
  private val _clz: Class[T] = m.erasure.asInstanceOf[Class[T]]

  protected val _dao: MyDAO[T, ObjectId] = new MyDAO[T, ObjectId](_clz, MongoDB.ds)
  protected def createQuery = _dao.createQuery

  protected def asList(q: QueryResults[T]) = q.asList.asScala

  def findById(id: ObjectId) : Option[T] = Option(_dao.get(id))
  def findById(id: String) : Option[T] = findById(new ObjectId(id))

  def findAll = asList(_dao.find)

  def count = _dao.count

  def deleteById(id: ObjectId) { _dao.deleteById(id) }
  def deleteById(id: String) { deleteById(new ObjectId(id)) }
  def deleteAll = _dao.deleteByQuery(_dao.createQuery)
  def drop = _dao.getCollection.drop
}