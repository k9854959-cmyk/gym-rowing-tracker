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
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Exercise(val name:String,val target:String,val cue:String,val avoid:String)
data class Workout(val title:String,val subtitle:String,val exercises:List<Exercise>,val guide:Int?)
data class WeightEntry(val date:Long,val kg:Double,val waist:Double?)

private val teal=Color(0xFF3F51B5); private val bg=Color(0xFFF4F5FF); private val coral=Color(0xFFFF7043)
private val fullBody=listOf(
 Exercise("Goblet squat","3 × 8–10","Chest tall; knees follow toes.","Knees collapsing inward"),
 Exercise("Dumbbell bench press","3 × 8–12","Lower with control; wrists stacked.","Elbows flared straight out"),
 Exercise("One-arm dumbbell row","3 × 10 each side","Brace and pull elbow toward hip.","Twisting the torso"),
 Exercise("Romanian deadlift","3 × 8–10","Push hips back; keep weights close.","Rounding the back"),
 Exercise("Plank","3 × 30–45 sec","Squeeze glutes; ribs down.","Hips sagging")
)
private val upperBody=listOf(
 Exercise("Dumbbell bench press","3 × 8–12","Lower with control; wrists stacked.","Elbows flared straight out"),
 Exercise("Lat pulldown","3 × 8–12","Pull elbows toward your ribs.","Swinging backward"),
 Exercise("Dumbbell shoulder press","3 × 8–12","Brace; press over shoulders.","Leaning far backward"),
 Exercise("Seated cable row","3 × 10–12","Lead with elbows; pause briefly.","Shrugging shoulders"),
 Exercise("Dumbbell lateral raise","2 × 12–15","Lift smoothly to shoulder height.","Using momentum"),
 Exercise("Dumbbell curl","2 × 10–15","Keep elbows still.","Swinging the weight"),
 Exercise("Triceps pushdown","2 × 10–15","Lock elbows beside your body.","Moving the shoulders")
)
private val lowerBody=listOf(
 Exercise("Leg press","3 × 8–12","Keep feet planted; control depth.","Locking knees hard"),
 Exercise("Romanian deadlift","3 × 8–10","Push hips back; keep weights close.","Rounding the back"),
 Exercise("Reverse lunge","3 × 8 each leg","Step back; keep front foot planted.","Front knee falling inward"),
 Exercise("Hip thrust","3 × 10–12","Finish by squeezing glutes.","Overarching the lower back"),
 Exercise("Standing calf raise","3 × 12–15","Pause high and lower fully.","Bouncing"),
 Exercise("Farmer carry","3 × 30–45 sec","Walk tall with steady steps.","Leaning to one side")
)
private val coreBody=listOf(
 Exercise("Dead bug","3 × 8 each side","Keep lower back gently down.","Rushing the movement"),
 Exercise("Side plank","3 × 20–40 sec each","Make a straight line head to heel.","Hips dropping"),
 Exercise("Pallof press","3 × 10 each side","Resist rotation; breathe out.","Twisting toward the cable"),
 Exercise("Bird dog","3 × 8 each side","Reach long without rotating.","Arching the back"),
 Exercise("Farmer carry","3 × 40 sec","Walk tall; brace naturally.","Holding your breath"),
 Exercise("Glute bridge","3 × 12","Drive through heels; squeeze glutes.","Overarching the back")
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
 fun recommendation(name:String,starter:Double)=Double.fromBits(p.getLong("rec_${name.hashCode()}",starter.toBits()))
 fun saveRecommendation(name:String,value:Double){p.edit().putLong("rec_${name.hashCode()}",value.coerceAtLeast(0.0).toBits()).apply()}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FitLoopApp(context:Context){
 val store=remember{Store(context)};var tab by remember{mutableIntStateOf(0)};var week by remember{mutableIntStateOf(store.week)};var day by remember{mutableIntStateOf(store.day)};var guide by remember{mutableStateOf<Int?>(null)};var addWeight by remember{mutableStateOf(false)}
 val workouts=remember(week){plan(week)}
 MaterialTheme(colorScheme=lightColorScheme(primary=teal,secondary=coral,background=bg,surface=Color.White)){
  Scaffold(containerColor=bg,bottomBar={NavigationBar(containerColor=Color.White){listOf("Today" to Icons.Default.Today,"Plan" to Icons.Default.CalendarMonth,"Progress" to Icons.Default.ShowChart).forEachIndexed{i,x->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(x.second,null)},label={Text(x.first)})}}}){pad->
   Column(Modifier.padding(pad).padding(horizontal=16.dp)){
    Spacer(Modifier.height(14.dp));Row(verticalAlignment=Alignment.CenterVertically){Text("FIT LOOP",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black,color=teal);Spacer(Modifier.width(8.dp));Surface(color=teal,shape=RoundedCornerShape(8.dp)){Text("V2",color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=8.dp,vertical=3.dp))}};Text("Stronger · Leaner · Consistent",color=Color.Gray);Spacer(Modifier.height(12.dp))
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

fun plan(week:Int):List<Workout>{val phase=if(week==1)"Foundation" else if(week==2)"Progress" else "Recovery";val steady=if(week==2)40 else if(week==3)30 else 35;return listOf(
 Workout("Full body","$phase · strength then 10–15 min easy row",fullBody,R.drawable.strength_a_guide),
 Workout("Upper body","$phase · strength then rowing intervals",upperBody,R.drawable.strength_a_guide),
 Workout("Lower body","$phase · strength then optional 10 min easy row",lowerBody,R.drawable.strength_b_guide),
 Workout("Core + conditioning","$phase · core then $steady min steady row",coreBody,R.drawable.rowing_guide))}

@Composable fun Today(w:Workout,week:Int,day:Int,store:Store,onGuide:(Int)->Unit,onComplete:()->Unit){var started by remember(w.title,week){mutableStateOf(false)};LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=18.dp)){
 item{Card(colors=CardDefaults.cardColors(containerColor=teal),shape=RoundedCornerShape(20.dp)){Column(Modifier.fillMaxWidth().padding(18.dp)){Text("CYCLE WEEK $week · WORKOUT ${day+1}",color=Color.White.copy(.75f));Text(w.title,color=Color.White,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(w.subtitle,color=Color.White)}};Spacer(Modifier.height(8.dp));if(!started){Button(onClick={started=true},Modifier.fillMaxWidth().height(58.dp)){Icon(Icons.Default.PlayArrow,null);Spacer(Modifier.width(8.dp));Text("Start workout",style=MaterialTheme.typography.titleMedium)}}else{GuidedTimer(startWithPrep=true)};w.guide?.let{OutlinedButton(onClick={onGuide(it)},Modifier.fillMaxWidth()){Icon(Icons.Default.MenuBook,null);Spacer(Modifier.width(8.dp));Text("Open picture guide")}}}
 if(started){itemsIndexed(w.exercises){i,e->ExerciseCard(e,week,store,store.logKey(week,day,i)){store.saveLog(week,day,i,it)}}
 item{Button(onClick={started=false;onComplete()},Modifier.fillMaxWidth().height(52.dp)){Icon(Icons.Default.CheckCircle,null);Spacer(Modifier.width(8.dp));Text("Finish workout")};Text("Complete all reps with 1–3 good reps left. Week 3 reduces fatigue.",Modifier.padding(8.dp),style=MaterialTheme.typography.bodySmall,color=Color.Gray)}}
 }}

@Composable fun ExerciseCard(e:Exercise,week:Int,store:Store,value:String,onChange:(String)->Unit){
 val starter=starterWeight(e.name);var recommended by remember(e.name){mutableDoubleStateOf(store.recommendation(e.name,starter))};var actual by remember(value){mutableStateOf(value)}
 Card(shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(14.dp)){Row{Column(Modifier.weight(1f)){Text(e.name,fontWeight=FontWeight.Bold);Text(if(week==3)e.target.replace("3 ×","2 ×") else e.target,color=teal)};Icon(Icons.Default.FitnessCenter,null,tint=coral)}
  if(starter>0){Spacer(Modifier.height(7.dp));Surface(color=Color(0xFFE3F2EF),shape=RoundedCornerShape(10.dp)){Column(Modifier.fillMaxWidth().padding(10.dp)){Text("RECOMMENDED WORKING WEIGHT",style=MaterialTheme.typography.labelSmall,color=teal);Text("${formatKg(if(week==3)recommended*.9 else recommended)} kg",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text("Adjust once if equipment increments differ.",style=MaterialTheme.typography.bodySmall)}}}
  Spacer(Modifier.height(7.dp));Text("Cue: ${e.cue}",style=MaterialTheme.typography.bodySmall);Text("Avoid: ${e.avoid}",style=MaterialTheme.typography.bodySmall,color=Color(0xFF9A4D3C));Spacer(Modifier.height(8.dp));OutlinedTextField(actual,{actual=it;onChange(it)},Modifier.fillMaxWidth(),label={Text(if(starter>0)"Actual kg × reps (example: 16 × 10,10,9)" else "Reps / time")},singleLine=true)
  if(starter>0){Text("How did the final set feel?",style=MaterialTheme.typography.labelMedium,modifier=Modifier.padding(top=8.dp));Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){AssistChip(onClick={recommended=nextIncrement(recommended,true);store.saveRecommendation(e.name,recommended)},label={Text("Easy +")});AssistChip(onClick={store.saveRecommendation(e.name,recommended)},label={Text("Right")});AssistChip(onClick={recommended=nextIncrement(recommended,false);store.saveRecommendation(e.name,recommended)},label={Text("Heavy −")})};Text("Easy increases next time; Right keeps it; Heavy reduces it.",style=MaterialTheme.typography.bodySmall,color=Color.Gray)}
 }}}

private fun starterWeight(name:String)=when(name){"Goblet squat"->12.0;"Dumbbell bench press"->8.0;"One-arm dumbbell row"->10.0;"Romanian deadlift"->16.0;"Lat pulldown"->25.0;"Dumbbell shoulder press"->6.0;"Seated cable row"->25.0;"Dumbbell lateral raise"->4.0;"Dumbbell curl"->6.0;"Triceps pushdown"->15.0;"Leg press"->40.0;"Reverse lunge"->8.0;"Hip thrust"->20.0;"Standing calf raise"->20.0;"Farmer carry"->12.0;else->0.0}
private fun nextIncrement(v:Double,up:Boolean):Double=(v+(if(up)if(v<10)1.0 else 2.0 else if(v<=10)-1.0 else -2.0)).coerceAtLeast(1.0)
private fun formatKg(v:Double)=if(v%1.0==0.0)v.toInt().toString() else "%.1f".format(v)

@Composable fun GuidedTimer(startWithPrep:Boolean=false){var seconds by remember{mutableIntStateOf(if(startWithPrep)300 else 0)};var running by remember{mutableStateOf(startWithPrep)};var label by remember{mutableStateOf(if(startWithPrep)"Preparation — let's begin" else "Ready")};LaunchedEffect(running,seconds){if(running&&seconds>0){delay(1000);seconds--}else if(running&&seconds==0)running=false};Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(16.dp),modifier=Modifier.padding(vertical=8.dp)){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(label,fontWeight=FontWeight.Bold);Text("%02d:%02d".format(seconds/60,seconds%60),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black,color=teal);if(seconds>0)Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){TextButton(onClick={running=!running}){Text(if(running)"Pause"else"Resume")};TextButton(onClick={seconds+=30}){Text("+30 sec")};TextButton(onClick={seconds=0;running=false;label="Ready for your first set"}){Text("Skip")}}else Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){AssistChip(onClick={label="Preparation";seconds=300;running=true},label={Text("5 min prep")});AssistChip(onClick={label="Rest — stay focused";seconds=90;running=true},label={Text("90 sec rest")});AssistChip(onClick={label="Cooldown — well done";seconds=300;running=true},label={Text("5 min cool")})}}}}

@Composable fun PlanScreen(ws:List<Workout>,week:Int,current:Int,onOpen:(Int)->Unit,onGuide:(Int)->Unit){Column{Text("Three-week loop",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text(if(week==1)"Foundation" else if(week==2)"Progress" else "Recovery",color=teal);Spacer(Modifier.height(8.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){itemsIndexed(ws){i,w->Card(Modifier.fillMaxWidth().clickable{onOpen(i)},colors=CardDefaults.cardColors(containerColor=if(i==current)Color(0xFFE3F2EF) else Color.White)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("${i+1}",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=teal);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(w.title,fontWeight=FontWeight.Bold);Text(w.subtitle,style=MaterialTheme.typography.bodySmall)};w.guide?.let{IconButton(onClick={onGuide(it)}){Icon(Icons.Default.Image,null)}}}}}}}}

@Composable fun Progress(weights:List<WeightEntry>,add:()->Unit,week:Int){Column{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Progress",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge);Text("Cycle week $week",color=teal)};Button(onClick=add){Icon(Icons.Default.Add,null);Text(" Weigh-in")}};Spacer(Modifier.height(12.dp));if(weights.isEmpty())Text("No weigh-ins yet. Add one weekly, under similar conditions.",color=Color.Gray)else LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){itemsIndexed(weights){i,e->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(SimpleDateFormat("d MMM yyyy",Locale.getDefault()).format(Date(e.date)),color=Color.Gray);Text("${e.kg} kg",fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)};e.waist?.let{Text("Waist\n$it cm")};if(i>0){val d=e.kg-weights[i-1].kg;Text("  ${if(d>0)"+" else ""}${"%.1f".format(d)} kg",color=if(d<=0)teal else coral)}}}}}}}

@Composable fun WeightDialog(close:()->Unit,save:(Double,Double?)->Unit){var kg by remember{mutableStateOf("")};var waist by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Weekly weigh-in")},text={Column{OutlinedTextField(kg,{kg=it.filter{x->x.isDigit()||x=='.'}},label={Text("Weight (kg)")});Spacer(Modifier.height(8.dp));OutlinedTextField(waist,{waist=it.filter{x->x.isDigit()||x=='.'}},label={Text("Waist cm (optional)")});Text("Use the same scale, around the same time each week.",style=MaterialTheme.typography.bodySmall,color=Color.Gray)}},confirmButton={TextButton(enabled=kg.toDoubleOrNull()!=null,onClick={save(kg.toDouble(),waist.toDoubleOrNull())}){Text("Save")}},dismissButton={TextButton(onClick=close){Text("Cancel")}})}

@Composable fun GuideDialog(res:Int,close:()->Unit){Dialog(onDismissRequest=close){Surface(shape=RoundedCornerShape(16.dp),color=Color.White){Column{Box(Modifier.weight(1f,false).heightIn(max=700.dp).verticalScroll(rememberScrollState())){Image(painterResource(res),"Exercise guide",Modifier.fillMaxWidth(),contentScale=ContentScale.FillWidth)};Button(onClick=close,Modifier.fillMaxWidth().padding(12.dp)){Text("Close guide")}}}}}
