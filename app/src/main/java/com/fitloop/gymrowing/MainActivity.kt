package com.fitloop.gymrowing

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Exercise(val name:String,val target:String,val cue:String,val avoid:String)
data class Workout(val title:String,val subtitle:String,val exercises:List<Exercise>,val guide:Int?)
data class WeightEntry(val date:Long,val kg:Double,val waist:Double?)

private val teal=Color(0xFF176B69); private val bg=Color(0xFFF8F5EF); private val coral=Color(0xFFE7654B)
private val strengthA=listOf(
 Exercise("Goblet squat","3 × 8–10","Chest tall; knees follow toes.","Knees collapsing inward"),
 Exercise("Dumbbell bench press","3 × 8–12","Lower with control; wrists stacked.","Elbows flared straight out"),
 Exercise("One-arm dumbbell row","3 × 10 each side","Brace and pull elbow toward hip.","Twisting the torso"),
 Exercise("Romanian deadlift","3 × 8–10","Push hips back; keep weights close.","Rounding the back"),
 Exercise("Plank","3 × 20–40 sec","Squeeze glutes; ribs down.","Hips sagging")
)
private val strengthB=listOf(
 Exercise("Reverse lunge","3 × 8 each leg","Step back; keep front foot planted.","Front knee falling inward"),
 Exercise("Dumbbell overhead press","3 × 8–12","Brace; press over shoulders.","Leaning far backward"),
 Exercise("Dumbbell deadlift","3 × 6–10","Drive the floor away; stand tall.","Jerking from the floor"),
 Exercise("Bent-over row","3 × 8–12","Hold hinge; pull elbows back.","Shrugging shoulders"),
 Exercise("Farmer carry","3 × 30–45 sec","Walk tall with steady steps.","Leaning to one side")
)

class MainActivity:ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{FitLoopApp(this)}} }

class Store(context:Context){
 private val p=context.getSharedPreferences("fit_loop",Context.MODE_PRIVATE)
 var week:Int get()=p.getInt("week",1);set(v){p.edit().putInt("week",v).apply()}
 var day:Int get()=p.getInt("day",0);set(v){p.edit().putInt("day",v).apply()}
 fun weights():List<WeightEntry>{return try{val a=JSONArray(p.getString("weights","[]"));buildList{for(i in 0 until a.length()){val o=a.getJSONObject(i);add(WeightEntry(o.getLong("date"),o.getDouble("kg"),if(o.has("waist"))o.getDouble("waist") else null))}}}catch(_:Exception){emptyList()}}
 fun addWeight(e:WeightEntry){val a=JSONArray();(listOf(e)+weights()).take(30).forEach{a.put(JSONObject().apply{put("date",it.date);put("kg",it.kg);it.waist?.let{w->put("waist",w)}})};p.edit().putString("weights",a.toString()).apply()}
 fun logKey(w:Int,d:Int,i:Int)=p.getString("log_${w}_${d}_$i","")?:""
 fun saveLog(w:Int,d:Int,i:Int,value:String){p.edit().putString("log_${w}_${d}_$i",value).apply()}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FitLoopApp(context:Context){
 val store=remember{Store(context)};var tab by remember{mutableIntStateOf(0)};var week by remember{mutableIntStateOf(store.week)};var day by remember{mutableIntStateOf(store.day)};var guide by remember{mutableStateOf<Int?>(null)};var addWeight by remember{mutableStateOf(false)}
 val workouts=remember(week){plan(week)}
 MaterialTheme(colorScheme=lightColorScheme(primary=teal,secondary=coral,background=bg,surface=Color.White)){
  Scaffold(containerColor=bg,bottomBar={NavigationBar(containerColor=Color.White){listOf("Today" to Icons.Default.Today,"Plan" to Icons.Default.CalendarMonth,"Progress" to Icons.Default.ShowChart).forEachIndexed{i,x->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(x.second,null)},label={Text(x.first)})}}}){pad->
   Column(Modifier.padding(pad).padding(horizontal=16.dp)){
    Spacer(Modifier.height(14.dp));Text("FIT LOOP",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black,color=teal);Text("Row · Lift · Repeat",color=Color.Gray);Spacer(Modifier.height(12.dp))
    when(tab){0->Today(workouts[day],week,day,store,{guide=it}){if(day<3){day++;store.day=day}else{day=0;week=if(week==3)1 else week+1;store.day=0;store.week=week}}
     1->PlanScreen(workouts,week,day,{day=it;store.day=it;tab=0},{guide=it})
     else->Progress(store.weights(),{addWeight=true},week)
    }
   }
  }
  guide?.let{GuideDialog(it){guide=null}}
  if(addWeight)WeightDialog({addWeight=false}){kg,waist->store.addWeight(WeightEntry(System.currentTimeMillis(),kg,waist));addWeight=false;tab=0;tab=2}
 }
}

fun plan(week:Int):List<Workout>{val sets=if(week==3)"2 sets" else "3 sets";val rounds=when(week){1->6;2->8;else->5};val steady=when(week){1->35;2->40;else->30};return listOf(
 Workout("Strength A + easy row","$sets · then ${if(week==2)20 else 15} min easy row",strengthA,R.drawable.strength_a_guide),
 Workout("Rowing intervals","5 min warm-up · $rounds rounds: 1 min hard / 2 min easy · 5 min cool-down",listOf(Exercise("Intervals","$rounds rounds","Hard but controlled; smooth strokes.","All-out sprinting")),R.drawable.rowing_guide),
 Workout("Strength B + easy row","$sets · then ${if(week==2)20 else 15} min easy row",strengthB,R.drawable.strength_b_guide),
 Workout("Steady row","$steady minutes at conversational pace",listOf(Exercise("Steady rowing","$steady minutes","Breathe steadily; relaxed recovery.","Starting too fast")),R.drawable.rowing_guide))}

@Composable fun Today(w:Workout,week:Int,day:Int,store:Store,onGuide:(Int)->Unit,onComplete:()->Unit){LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=18.dp)){
 item{Card(colors=CardDefaults.cardColors(containerColor=teal),shape=RoundedCornerShape(20.dp)){Column(Modifier.fillMaxWidth().padding(18.dp)){Text("CYCLE WEEK $week · WORKOUT ${day+1}",color=Color.White.copy(.75f));Text(w.title,color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(w.subtitle,color=Color.White)}};Spacer(Modifier.height(4.dp));w.guide?.let{OutlinedButton(onClick={onGuide(it)},Modifier.fillMaxWidth()){Icon(Icons.Default.MenuBook,null);Spacer(Modifier.width(8.dp));Text("Open picture guide")}}}
 itemsIndexed(w.exercises){i,e->ExerciseCard(e,store.logKey(week,day,i)){store.saveLog(week,day,i,it)}}
 item{Button(onClick=onComplete,Modifier.fillMaxWidth().height(52.dp)){Icon(Icons.Default.CheckCircle,null);Spacer(Modifier.width(8.dp));Text("Finish workout")};Text("Complete all reps with 2–3 good reps left. Week 3 reduces fatigue.",Modifier.padding(8.dp),style=MaterialTheme.typography.bodySmall,color=Color.Gray)}
 }}

@Composable fun ExerciseCard(e:Exercise,value:String,onChange:(String)->Unit){Card(shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Row{Column(Modifier.weight(1f)){Text(e.name,fontWeight=FontWeight.Bold);Text(e.target,color=teal)};Icon(Icons.Default.FitnessCenter,null,tint=coral)};Spacer(Modifier.height(7.dp));Text("Cue: ${e.cue}",style=MaterialTheme.typography.bodySmall);Text("Avoid: ${e.avoid}",style=MaterialTheme.typography.bodySmall,color=Color(0xFF9A4D3C));Spacer(Modifier.height(8.dp));OutlinedTextField(value,onChange,Modifier.fillMaxWidth(),label={Text("Weight / reps / distance")},singleLine=true)}}}

@Composable fun PlanScreen(ws:List<Workout>,week:Int,current:Int,onOpen:(Int)->Unit,onGuide:(Int)->Unit){Column{Text("Three-week loop",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(if(week==1)"Foundation" else if(week==2)"Progress" else "Recovery",color=teal);Spacer(Modifier.height(8.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){itemsIndexed(ws){i,w->Card(Modifier.fillMaxWidth().clickable{onOpen(i)},colors=CardDefaults.cardColors(containerColor=if(i==current)Color(0xFFE3F2EF) else Color.White)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("${i+1}",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=teal);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(w.title,fontWeight=FontWeight.Bold);Text(w.subtitle,style=MaterialTheme.typography.bodySmall)};w.guide?.let{IconButton(onClick={onGuide(it)}){Icon(Icons.Default.Image,null)}}}}}}}}

@Composable fun Progress(weights:List<WeightEntry>,add:()->Unit,week:Int){Column{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Progress",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text("Cycle week $week",color=teal)};Button(onClick=add){Icon(Icons.Default.Add,null);Text(" Weigh-in")}};Spacer(Modifier.height(12.dp));if(weights.isEmpty())Text("No weigh-ins yet. Add one weekly, under similar conditions.",color=Color.Gray)else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){itemsIndexed(weights){i,e->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(SimpleDateFormat("d MMM yyyy",Locale.getDefault()).format(Date(e.date)),color=Color.Gray);Text("${e.kg} kg",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)};e.waist?.let{Text("Waist\n$it cm")};if(i>0){val d=e.kg-weights[i-1].kg;Text("  ${if(d>0)"+" else ""}${"%.1f".format(d)} kg",color=if(d<=0)teal else coral)}}}}}}}

@Composable fun WeightDialog(close:()->Unit,save:(Double,Double?)->Unit){var kg by remember{mutableStateOf("")};var waist by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Weekly weigh-in")},text={Column{OutlinedTextField(kg,{kg=it.filter{x->x.isDigit()||x=='.'}},label={Text("Weight (kg)")});Spacer(Modifier.height(8.dp));OutlinedTextField(waist,{waist=it.filter{x->x.isDigit()||x=='.'}},label={Text("Waist cm (optional)")});Text("Use the same scale, around the same time each week.",style=MaterialTheme.typography.bodySmall,color=Color.Gray)}},confirmButton={TextButton(enabled=kg.toDoubleOrNull()!=null,onClick={save(kg.toDouble(),waist.toDoubleOrNull())}){Text("Save")}},dismissButton={TextButton(onClick=close){Text("Cancel")}})}

@Composable fun GuideDialog(res:Int,close:()->Unit){Dialog(onDismissRequest=close){Surface(shape=RoundedCornerShape(16.dp),color=Color.White){Column{Box(Modifier.weight(1f,false).heightIn(max=700.dp).verticalScroll(rememberScrollState())){Image(painterResource(res),"Exercise guide",Modifier.fillMaxWidth(),contentScale=ContentScale.FillWidth)};Button(onClick=close,Modifier.fillMaxWidth().padding(12.dp)){Text("Close guide")}}}}}
